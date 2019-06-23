/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.RequiresComponent;
import com.sk89q.rebar.components.GuestProtectedEvent;
import com.sk89q.rebar.event.EventManager;
import com.sk89q.rebar.event.RegisteredEvent;
import com.sk89q.rebar.util.CommandUtil;

@RequiresComponent(WhitelistEnforcer.class)
public class CreativeServer extends AbstractComponent {

    @Override
    public void initialize() {
        WorldCreator worldCreator = new WorldCreator("testprivate");
        worldCreator.generator(new TestWorldGenerator(false));
        worldCreator.environment(Environment.NORMAL);
        Rebar.getInstance().getServer().createWorld(worldCreator);

        worldCreator = new WorldCreator("testpublic");
        worldCreator.generator(new TestWorldGenerator(true));
        worldCreator.environment(Environment.NORMAL);
        Rebar.getInstance().getServer().createWorld(worldCreator);

        Rebar.getInstance().registerCommands(CreativeCommands.class, this);
        Rebar.getInstance().registerEvents(new ProtectionListener());
        Rebar.getInstance().registerEvents(new EntityListener());
    }

    @Override
    public void shutdown() {
    }

    private static void teleport(Player player, String worldName) throws CommandException {
        World world = Rebar.getInstance().getServer().getWorld(worldName);
        if (world == null) {
            throw new CommandException("No world by the provided name could be found!");
        }
        player.teleport(world.getSpawnLocation());
    }

    public class EntityListener implements Listener {
        @EventHandler
        public void onFoodLevelChange(FoodLevelChangeEvent event) {
            if (event.getEntity() instanceof Player) {
                event.setFoodLevel(20);
            }
        }
    }

    public static class CreativeCommands {
        public CreativeCommands(CreativeServer component) {
        }

        @Command(aliases = { "world" }, desc = "Switch to the private test world")
        public void world(CommandContext context, CommandSender sender) throws CommandException {
            teleport(CommandUtil.checkPlayer(sender), "world");
            sender.sendMessage(ChatColor.AQUA +
                    "You are now on the PRIVATE overworld.");

            // Tell the user whether s/he has permission
            if (!Rebar.getInstance().hasPermission(sender, "build")) {
                sender.sendMessage(ChatColor.RED +
                        "You do not have access to this world. Please ask sk89q for access. " +
                        "In the mean time, please use /testpublic");
            } else {
                sender.sendMessage(ChatColor.GRAY +
                        "ABSOLUTELY NO TESTS ARE ALLOWED HERE. Please use /testprivate or /testpublic");
            }
        }

        @Command(aliases = { "testprivate" }, desc = "Switch to the private test world")
        public void testPrivate(CommandContext context, CommandSender sender) throws CommandException {
            teleport(CommandUtil.checkPlayer(sender), "testprivate");
            sender.sendMessage(ChatColor.AQUA +
                    "You are now on the PRIVATE test world.");

            // Tell the user whether s/he has permission
            if (!Rebar.getInstance().hasPermission(sender, "build")) {
                sender.sendMessage(ChatColor.RED +
                        "You do not have access to this world. " +
                        "Please ask sk89q for access. In the mean time, please use /testpublic");
            } else {
                sender.sendMessage(ChatColor.GRAY +
                        "Please mark your tests with your name using a sign.");
            }
        }

        @Command(aliases = { "testpublic" }, desc = "Switch to the public test world")
        public void testPublic(CommandContext context, CommandSender sender) throws CommandException {
            teleport(CommandUtil.checkPlayer(sender), "testpublic");
            sender.sendMessage(ChatColor.AQUA +
                    "You are now on the PUBLIC test world. ");
            sender.sendMessage(ChatColor.GRAY +
                    "Please mark your tests with your name using a sign.");
        }

    }

    public class ProtectionListener extends com.sk89q.rebar.components.ProtectionListener {
        @Override
        @RegisteredEvent(type = GuestProtectedEvent.class, priority = EventManager.PRIORITY_LOWEST)
        public void onEvent(GuestProtectedEvent event) {
            if (event.isCancelled()) {
                event.setMessage(ChatColor.RED + "You don't have permission for this world. " +
                		"Please ask sk89q for access. In the mean time, try /testpublic");
            }
        }
    }

}
