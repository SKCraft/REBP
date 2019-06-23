/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.network;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import groovy.lang.GroovyClassLoader;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.comphenix.protocol.Packets.getDeclaredName;

public class PacketDictator extends AbstractComponent {

    private static final String PACKET_CLASS_NAME = "ef";
    private static final String PACKET_CLASS_GET_SIZE_NAME = "a";

    private final Logger logger = createLogger(PacketDictator.class);
    private final Timer timer = new Timer("Rebar PacketDictator");
    private final Map<Player, ProfileData> profileData = new LinkedHashMap<Player, ProfileData>();

    private Method getPacketSize;
    private File monitorScript;
    private PacketMonitor monitor;

    @Override
    public void shutdown() {
    }

    @Override
    public void initialize() {
        monitorScript = new File(Rebar.getInstance().getDataFolder(), "handlers/CustomPacketHandler.groovy");
        timer.scheduleAtFixedRate(new StatsTimer(), 0, 5000);
        Rebar.getInstance().registerCommands(PacketDictatorCommands.class, this);
        findMethods();
        loadHandler();
        registerPacketListener();
    }

    private void findMethods() {
        try {
            Class<?> packetClass = Class.forName(PACKET_CLASS_NAME);
            Method method = packetClass.getMethod(PACKET_CLASS_GET_SIZE_NAME);
            getPacketSize = method;
        } catch (Throwable t) {
            logger.log(Level.WARNING, "Failed to get getPacketSize()", t);
        }
    }

    private void registerPacketListener() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(Rebar.getInstance(),
                ConnectionSide.SERVER_SIDE, ListenerPriority.NORMAL) {

            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                Player player = event.getPlayer();

                // Send off to Groovy script
                PacketMonitor mon = monitor; // Atomic

                if (mon != null) {
                    try {
                        mon.onPacketSending(event);
                    } catch (Throwable t) {
                        logger.log(Level.WARNING, "Failed to run packet monitor", t);
                    }
                }

                // Skip some packets from being processed
                if (packet.getID() == Packets.Server.CHAT || packet.getID() == Packets.Server.KEEP_ALIVE) {
                    return;
                }

                // Get the data
                ProfileData data = null;

                synchronized (profileData) {
                    for (Map.Entry<Player, ProfileData> entry : profileData.entrySet()) {
                        if (entry.getKey().getAddress().equals(player.getAddress())) {
                            data = entry.getValue();
                            break;
                        }
                    }
                }

                // Handle the user's current preferences
                if (data != null) {
                    data.add(getPacketName(packet), getPacketSize(packet));

                    // Choose to drop
                    if (data.isDroppingCustom()) {
                        event.setCancelled(true);
                    }
                }
            }

        });
    }

    /**
     * Get the size of the packet.
     *
     * @param packet the packet
     * @return packet size
     */
    public int getPacketSize(PacketContainer packet) {
        if (getPacketSize == null) {
            return 0; // Boo!
        } else {
            try {
                return (Integer) getPacketSize.invoke(packet.getHandle());
            } catch (Throwable t) {
                getPacketSize = null;
                logger.log(Level.SEVERE, "Failed to getPacketSize() out of the blue", t);
                return 0;
            }
        }
    }

    /**
     * Return an identifiable name for the packet.
     *
     * @param packet the packet
     * @return name
     */
    public String getPacketName(PacketContainer packet) {
        int id = packet.getID();

        String declaredName = getDeclaredName(id);
        StringBuilder name = new StringBuilder();

        // Friendly name
        if (declaredName != null) {
            name.append(declaredName);
        }

        // Start with the ID
        name.append("#");
        name.append(id);

        // Add the custom playing load name to the name
        if (id == Packets.Server.CUSTOM_PAYLOAD) {
            String type = packet.getStrings().read(0);
            name.append("_");
            name.append(type);
        }

        return name.toString();
    }

    /**
     * Reload the Groovy packet monitor script.
     */
    public void loadHandler() {
        if (monitorScript.exists()) {
            GroovyClassLoader gcl = new GroovyClassLoader(getClass().getClassLoader());
            try {
                Class clazz = gcl.parseClass(monitorScript);
                Object obj = clazz.newInstance();
                PacketMonitor monitor = (PacketMonitor) obj;
                this.monitor = monitor;
                logger.info("PacketDictator: Loaded packet monitor script!");
            } catch (Throwable e) {
                logger.log(Level.WARNING, "Failed to load packet monitor script", e);
            }
        } else {
            logger.info("PacketDictator: Removed packet monitor script");
            monitor = null;
        }
    }

    public ProfileData startLogging(Player player, boolean intervalPrinting) {
        synchronized (profileData) {
            ProfileData data = new ProfileData();
            data.setIntervalPrinting(intervalPrinting);
            profileData.put(player, data);
            return data;
        }
    }

    public ProfileData stopLogging(Player player) {
        synchronized (profileData) {
            ProfileData data = profileData.get(player);
            profileData.remove(player);
            return data;
        }
    }

    public void printResults(ProfileData.Result result, CommandSender sender) {
        List<UsageCount> results = result.getResults();
        long elapsed = result.getElapsedTime();

        Collections.sort(results);

        if (results.size() > 0) {
            sender.sendMessage(ChatColor.GRAY + "------------------------------");

            for (UsageCount count : results) {
                String id = count.getId();
                double rate = count.getTotalBytes() / (double) elapsed;
                double roundedRate = Math.round(rate * 10000) / 10000.0;
                sender.sendMessage(
                        ChatColor.GRAY + "" + id + " " +
                        ChatColor.YELLOW +
                        roundedRate + ChatColor.GRAY + "B/s " +
                        ChatColor.GREEN +
                        count.getTotalBytes() + ChatColor.GRAY + "B " +
                        ChatColor.AQUA +
                        count.getPacketCount());
            }
        }
    }

    private class StatsTimer extends TimerTask {
        private long lastTime = 0;

        @Override
        public void run() {
            if (lastTime == 0) {
                lastTime = System.currentTimeMillis();
                return;
            }

            long now = System.currentTimeMillis();
            double elapsed = now - lastTime;

            synchronized (profileData) {
                Iterator<Map.Entry<Player, ProfileData>> it =
                        profileData.entrySet().iterator();

                while (it.hasNext()) {
                    Map.Entry<Player, ProfileData> entry = it.next();
                    Player player = entry.getKey();

                    if (player.isOnline()) {
                        // Print data occasionally
                        if (entry.getValue().isIntervalPrinting()) {
                            printResults(entry.getValue().takeResults(), player);
                        }
                    } else {
                        it.remove(); // Player left
                    }
                }
            }

            lastTime = now;
        }
    }

    public static class PacketDictatorCommands {
        private PacketDictator pd;

        public PacketDictatorCommands(PacketDictator pd) {
            this.pd = pd;
        }

        @Command(aliases = {"reloadpmon"}, min = 0, max = 0, desc = "Reload the packet monitor script")
        @CommandPermissions("skcraft.network.reload-script")
        public void reloadScript(CommandContext context, CommandSender sender) throws CommandException {
            pd.loadHandler();
            ChatUtil.msg(sender, ChatColor.YELLOW + "Reloaded!");
        }

        @Command(aliases = {"netprof"}, min = 1, max = 1, desc = "Profile network usage", flags = "di")
        @CommandPermissions("skcraft.network.profile")
        public void profile(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            String arg = context.getString(0);

            if (arg.equalsIgnoreCase("on")) {
                ProfileData data = pd.startLogging(player, context.hasFlag('i'));

                // Drop custom
                if (context.hasFlag('d')) {
                    data.setDroppingCustom(true);
                }

                ChatUtil.msg(player, ChatColor.YELLOW + "Profiling has been enabled. Use " +
                        ChatColor.AQUA + "/netprof off" + ChatColor.YELLOW + " to turn off.");
            } else if (arg.equalsIgnoreCase("off")) {
                ProfileData data = pd.stopLogging(player);
                ChatUtil.msg(player, ChatColor.YELLOW + "Profiling has been disabled!");
                if (data != null) {
                    pd.printResults(data.takeResults(), player);
                }
            } else {
                throw new CommandException("/netprof [-id] off | on");
            }
        }
    }

}
