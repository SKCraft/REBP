/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationNode;

public class WorldManagement extends AbstractComponent {

    private static final Logger logger = Logger.getLogger(WorldManagement.class.getCanonicalName());

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(WorldManagementCommands.class, this);

        for (ConfigurationNode worldNode : Rebar.getInstance().getRebarConfiguration().getNodeList("worlds", null)) {
            String name = worldNode.getString("name");
            String envName = worldNode.getString("environment", "overworld");
            if (name != null) {
                Environment env = null;

                if (envName.equalsIgnoreCase("overworld")) {
                    env = Environment.NORMAL;
                } else {
                    try {
                        env = Environment.valueOf(envName);
                    } catch (IllegalArgumentException e) {
                        logger.warning("WorldManagement: Couldn't load " + name + "; don't know what environment " + envName + " is!");
                        return;
                    }
                }

                logger.info("WorldManagement: Auto-loading world " + name + "...");
                Rebar.server().createWorld(WorldCreator.name(name).environment(env));
            }
        }
    }

    @Override
    public void shutdown() {
    }

    public static class WorldManagementCommands {
        public WorldManagementCommands(WorldManagement component) {
        }

        @Command(aliases = { "mount" }, desc = "Mount a world", min = 1, max = 1)
        @CommandPermissions({ "skcraft.worlds.mount" })
        public void mount(CommandContext context, CommandSender sender) throws CommandException {
            Server server = Rebar.getInstance().getServer();
            String worldName = context.getString(0).replaceAll("[^A-Za-z0-9_\\\\-]", "");
            World world = server.getWorld(worldName);

            // Check if the world has already been loaded
            if (world != null) {
                throw new CommandException("The world '" + worldName + "' is already loaded.");
            }

            // Don't want to load a world that doesn't exist
            if (!new File(worldName, "level.dat").exists()) {
                throw new CommandException("The world '" + worldName + "' does not already exist.");
            }

            server.broadcastMessage("A world is being mounted. Please wait.");
            server.createWorld(WorldCreator.name(worldName));
            server.broadcastMessage("The world has finished being mounted.");
            sender.sendMessage(ChatColor.YELLOW + "The world '" + worldName + "' has been mounted.");
        }

        @Command(aliases = { "unmount" }, desc = "Unmount a world", min = 1, max = 1)
        @CommandPermissions({ "skcraft.worlds.unmount" })
        public void unmount(CommandContext context, CommandSender sender) throws CommandException {
            Server server = Rebar.getInstance().getServer();
            String worldName = context.getString(0).replaceAll("[^A-Za-z0-9_\\\\-]", "");
            World world = server.getWorld(worldName);
            World main = server.getWorlds().get(0);
            Location target = main.getSpawnLocation();

            // Can't unload the main world
            if (main.equals(world)) {
                throw new CommandException("The main world cannot be unloaded.");
            }

            // Check if the world has been loaded to begin with
            if (world == null) {
                throw new CommandException("The world '" + worldName + "' was never loaded.");
            }

            // Move everyone off of it
            for (Player player : world.getPlayers()) {
                player.teleport(target);
                player.sendMessage(ChatColor.YELLOW + "You are being moved this world because it is being unloaded.");
            }

            server.unloadWorld(world, true);
            sender.sendMessage(ChatColor.YELLOW + "The world '" + worldName + "' has been unmounted.");
        }

        @Command(aliases = { "worlds" }, desc = "List all the loaded worlds", min = 0, max = 0)
        @CommandPermissions({ "skcraft.worlds.list" })
        public void list(CommandContext context, CommandSender sender) throws CommandException {
            Server server = Rebar.getInstance().getServer();

            for (World world : server.getWorlds()) {
                ChunkGenerator gen =  world.getGenerator();
                String genName = gen == null ? "BUILT-IN" : gen.getClass().getName();
                sender.sendMessage(ChatColor.LIGHT_PURPLE + world.getName() +
                        " " + ChatColor.BLUE + world.getEnvironment().name() +
                        " " + ChatColor.GREEN + genName);
            }
        }

    }

}
