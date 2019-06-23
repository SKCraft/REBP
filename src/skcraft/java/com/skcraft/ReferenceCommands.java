/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;

public class ReferenceCommands extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class Commands {
        public Commands(ReferenceCommands component) {
        }

        @Command(aliases = { "nethercoords" }, desc = "Get nether coordinates")
        public void netherCoords(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            World world = player.getWorld();
            Location loc = player.getLocation();

            if (world.getEnvironment() == Environment.NORMAL) {
                ChatUtil.msg(sender, ChatColor.LIGHT_PURPLE, "OVERWORLD: You are at ("
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
                ChatUtil.msg(sender, ChatColor.LIGHT_PURPLE, "If you want a portal in nether, make it as close as possible to: ("
                        + (loc.getBlockX() / 8) + ", " + loc.getBlockY() + ", " + (loc.getBlockZ() / 8) + ")");
            } else if (world.getEnvironment() == Environment.NETHER) {
                ChatUtil.msg(sender, ChatColor.LIGHT_PURPLE, "NETHER: You are at ("
                        + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + ")");
                ChatUtil.msg(sender, ChatColor.LIGHT_PURPLE, "If you want a portal in overworld, make it as close as possible to: ("
                        + (loc.getBlockX() * 8) + ", " + loc.getBlockY() + ", " + (loc.getBlockZ() * 8) + ")");
            } else {
                throw new CommandException("No calculations available for your world type.");
            }
        }

    }

}
