/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft.mappad;

import com.skcraft.mappad.apps.*;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.rebar.util.ChatUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapPad extends AbstractComponent {

    private static final Pattern codePattern = Pattern.compile("@([A-Za-z0-9]+)@");
    @InjectComponent private Sessions sessions;
    private final MapPad mapPad = this;
    private final ApplicationManager apps = new ApplicationManager(this);
    private final MapPadRenderer renderer = new MapPadRenderer(this);

    public MapPadRenderer getRenderer() {
        return renderer;
    }

    public MapPadSession getSession(Player player) {
        MapPadSession session = sessions.get(player, MapPadSession.class);
        if (session.getApplication() == null) {
            session.setApplication(new MainScreen(this, player));
        }
        return session;
    }

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new ServerListener());
        Rebar.getInstance().registerEvents(new PlayerListener());

        apps.register("home", MainScreen.class);
        apps.register("imgur", ImgurBrowser.class);
        apps.register("weather", WeatherApp.class);
        apps.register("scview", SignCodeViewer.class);
        apps.register("homing", HomingSignal.class);

        MapView map = Rebar.getInstance().getServer().getMap((short) 20);
        if (map != null) {
            setupMap(map);
        }
    }

    @Override
    public void shutdown() {
    }

    public static void print(Player player, String message) {
        ChatUtil.msg(player, ChatColor.AQUA, message);
    }

    public static void printError(Player player, String message) {
        ChatUtil.msg(player, ChatColor.RED, message);
    }

    private void setupMap(MapView map) {
        for (MapRenderer renderer : map.getRenderers()) {
            map.removeRenderer(renderer);
        }
        map.addRenderer(renderer);
    }

    public class ServerListener implements Listener {
        @EventHandler
        public void onMapInitialize(MapInitializeEvent event) {
            if (event.getMap().getId() == 20) {
                setupMap(event.getMap());
            }
        }
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerInteract(PlayerInteractEvent event) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();

            // Detect SignCodes
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isHoldingMapPad(player) && BlockUtil.isSign(block)) {
                Sign sign = BlockUtil.getState(block, Sign.class);

                for (String line : sign.getLines()) {
                    Matcher m = codePattern.matcher(line);

                    if (m.matches()) {
                        String id = m.group(1);
                        MapPadSession session = getSession(player);
                        Application currentApp = session.getApplication();

                        if (currentApp instanceof SignCodeViewer) {
                            SignCodeViewer signCodeApp = ((SignCodeViewer) currentApp);
                            signCodeApp.load(id);
                        } else {
                            session.replaceApplication(new SignCodeViewer(mapPad, player, id));
                        }
                    }
                }
            }
        }

        @EventHandler
        public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            String message = event.getMessage().trim();

            if (!isHoldingMapPad(player)) return;
            if (message.charAt(0) != '>' || message.length() < 2) return;

            event.setCancelled(true);

            ItemStack item = player.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("MapPad 2");
            player.setItemInHand(item);

            // Tell the user the original command
            MapPad.print(player, "$ " + message.substring(1).trim());

            MapPadSession session = getSession(player);
            Application currentApp = session.getApplication();

            // Process the command
            try {
                CommandContext context = new CommandContext(message.substring(1));
                String name = context.getCommand();

                if (apps.hasApp(name)) {
                    try {
                        Application app = apps.create(name, player);
                        session.replaceApplication(app);
                    } catch (ApplicationException e) {
                        MapPad.printError(player, "Failed to load app " + name + "!");
                    }
                } else if (context.getCommand().equalsIgnoreCase("apps")) {
                    MapPad.print(player, buildAppsList());
                } else {
                    currentApp.accept(context);
                }
            } catch (NumberFormatException e) {
                MapPad.printError(player, "error: Expected a number but got text");
            } catch (CommandException e) {
                MapPad.printError(player,  "error: " + e.getMessage());
            }
        }
    }

    private String buildAppsList() {
        StringBuilder str = new StringBuilder("Apps:");
        for (String name : apps.getAppNames()) {
            str.append(" >" + name);
        }
        return str.toString();
    }

    public static boolean isHoldingMapPad(Player player) {
        return player.getItemInHand().getType() == Material.MAP
                && player.getItemInHand().getDurability() == 20;
    }

}
