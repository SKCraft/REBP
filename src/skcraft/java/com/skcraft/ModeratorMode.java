/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.google.common.io.Files;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.LazyPluginReference;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.helpers.InjectPlugin;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import com.sk89q.rebar.util.InventorySerializer;
import com.skcraft.channels.Emotes;
import com.skcraft.security.ClientIdentityVerifier;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class ModeratorMode extends AbstractComponent {

    private static final Logger logger = createLogger(ModeratorMode.class);
    private File modInvenFolder;
    @InjectPlugin(WorldGuardPlugin.class)
    private LazyPluginReference<WorldGuardPlugin> wgRef;
    @InjectComponent
    private OpMode opMode;
    @InjectComponent
    private ClientIdentityVerifier verifier;
    private Map<UUID, PlayerState> active = new HashMap<UUID, PlayerState>();
    private File playersFolder;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
        Rebar.getInstance().registerEvents(new PlayerListener());

        Server server = Rebar.server();
        playersFolder = new File(server.getWorlds().get(0).getWorldFolder(), "playerdata");
        File[] files = playersFolder.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.getName().matches("^.*\\.backup\\.dat$")) {
                    logger.info("Moderator Mode: Found backup " + file.getAbsolutePath());
                    File playerFile = new File(playersFolder, file.getName().replaceAll("\\.backup\\.dat$", ".dat"));

                    try {
                        Files.copy(file, playerFile);
                        file.delete();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        modInvenFolder = new File(Rebar.getInstance().getDataFolder(), "modinven");
        modInvenFolder.mkdirs();
    }

    @Override
    public void shutdown() {
    }

    public boolean isActive(Player player) {
        return active.containsKey(player.getUniqueId());
    }

    public PlayerState getSession(Player player) {
        return active.get(player.getUniqueId());
    }

    @SuppressWarnings("deprecation")
    public void enter(Player player) throws CommandException {
        if (active.containsKey(player.getUniqueId()))
            throw new CommandException("You're already in moderator mode.");

        active.put(player.getUniqueId(), new PlayerState(player));

        player.setGameMode(GameMode.CREATIVE);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setFireTicks(0);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        File invenFile = new File(modInvenFolder, player.getUniqueId() + ".dat");
        if (invenFile.exists()) {
            try {
                InventorySerializer.read(invenFile, player.getInventory());
            } catch (IOException e) {
                e.printStackTrace();
                throw new CommandException("Failed to read mod inventory backup!");
            }
        }

        if (!player.isOp()) {
            opMode.setOpMode(player);
        }
    }

    @SuppressWarnings("deprecation")
    public void exit(Player player) throws CommandException {
        if (!active.containsKey(player.getUniqueId()))
            throw new CommandException("You're not in moderator mode.");

        PlayerState state = active.get(player.getUniqueId());

        if (opMode.hasOpMode(player)) {
            //opMode.unsetOpMode(player);
        }

        File invenFile = new File(modInvenFolder, player.getUniqueId() + ".dat");
        try {
            InventorySerializer.write(invenFile, player.getInventory());
        } catch (IOException e) {
            invenFile.delete();
            e.printStackTrace();
            throw new CommandException("Failed to write mod inventory backup!");
        }

        state.apply();
        active.remove(player.getUniqueId());
    }

    private class PlayerState {
        private Player player;
        private GameMode gameMode;
        private Location loc;
        private double health;
        private float saturationLevel;
        private int foodLevel;
        private float exhaustionLevel;
        private int fireTicks;
        private ItemStack[] inventory;
        private ItemStack helmet;
        private ItemStack chestplate;
        private ItemStack leggings;
        private ItemStack boots;
        private File backupFile;
        private boolean identified = false;

        public PlayerState(Player player) {
            this.player = player;

            gameMode = player.getGameMode();
            loc = player.getLocation();
            health = player.getHealth();
            saturationLevel = player.getSaturation();
            foodLevel = player.getFoodLevel();
            exhaustionLevel = player.getExhaustion();
            fireTicks = player.getFireTicks();

            PlayerInventory curInven = player.getInventory();
            inventory = curInven.getContents();
            helmet = curInven.getHelmet();
            chestplate = curInven.getChestplate();
            leggings = curInven.getLeggings();
            boots = curInven.getBoots();

            Server server = Rebar.server();
            String baseFilename = player.getUniqueId().toString();
            File playerFile = new File(playersFolder, baseFilename + ".dat");
            this.backupFile = new File(playersFolder, baseFilename + ".backup.dat");

            player.saveData();
            this.backupFile.delete();

            try {
                Files.copy(playerFile, this.backupFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isIdentified() {
            return identified;
        }

        public void setIdentified(boolean identified) {
            this.identified = identified;
            if (identified) {
                player.setDisplayName(Emotes.Images.SHIELD + player.getDisplayName());
            } else {
                player.setDisplayName(Emotes.stripEmotes(player.getDisplayName()));
            }
        }

        public void apply() {
            setIdentified(false);

            player.setGameMode(gameMode);
            player.setFallDistance(0);
            player.setHealth(health);
            player.setSaturation(saturationLevel);
            player.setFoodLevel(foodLevel);
            player.setExhaustion(exhaustionLevel);
            player.setFireTicks(fireTicks);
            player.teleport(loc);

            PlayerInventory curInven = player.getInventory();
            curInven.setContents(inventory);
            curInven.setHelmet(helmet);
            curInven.setChestplate(chestplate);
            curInven.setLeggings(leggings);
            curInven.setBoots(boots);

            this.backupFile.delete();
        }
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            Player player = event.getPlayer();
            if (isActive(player)) {
                try {
                    exit(player);
                } catch (CommandException e) {
                }
            }
        }
    }

    public static class Commands {
        private ModeratorMode component;

        public Commands(ModeratorMode component) {
            this.component = component;
        }

        @Command(aliases = {"mod"}, desc = "Enter moderator mode",
                 usage = "", min = 0, max = 1)
        @CommandPermissions({"skcraft.mod-mode"})
        public void enter(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);

            if (!component.verifier.isVerified(player)) {
                logger.warning("Moderator mode: Bad authorization bucket: " + player.getName());
                throw new CommandException("You are not in the right authorization bucket for this request.");
            }

            component.enter(player);

            ChatUtil.msg(sender, ChatColor.YELLOW, "You're now in moderator mode and your inventory was saved. " +
            		"Use /done to return to player mode.");

            if (context.argsLength() == 1) {
                Player target = CommandUtil.matchSinglePlayer(sender, context.getString(0));
                player.teleport(target);
            }
        }

        @Command(aliases = {"identifymod"}, desc = "Toggle moderator identification",
                 usage = "", min = 0, max = 0)
        @CommandPermissions({"skcraft.mod-mode"})
        public void identifyMod(CommandContext context, CommandSender sender) throws CommandException {
            PlayerState session = component.getSession(CommandUtil.checkPlayer(sender));
            if (session != null) {
                if (session.isIdentified()) {
                    session.setIdentified(false);
                    ChatUtil.msg(sender, ChatColor.YELLOW, "You now don't look like a mod.");
                } else {
                    session.setIdentified(true);
                    ChatUtil.msg(sender, ChatColor.YELLOW, "You now do look like a mod.");
                }
            } else {
                throw new CommandException("You're not in mod mode!");
            }
        }

        @Command(aliases = {"done"}, desc = "Leave moderator mode",
                 usage = "", min = 0, max = 0)
        @CommandPermissions({"skcraft.mod-mode"})
        public void leave(CommandContext context, CommandSender sender) throws CommandException {
            component.exit(CommandUtil.checkPlayer(sender));
            ChatUtil.msg(sender, ChatColor.YELLOW, "You're now a regular player.");
        }

    }

}
