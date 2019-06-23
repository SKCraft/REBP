/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.playblock;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;

public class PlayBlock extends AbstractComponent implements Listener {
    private MediaManager manager;

    @Override
    public void initialize() {
        manager = new MediaManager(new File(Rebar.getInstance().getDataFolder(), "playblock/state.json"));
        manager.load();
        Rebar.getInstance().registerCommands(PlayBlockCommands.class, this);
        Rebar.getInstance().registerEvents(this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(Rebar.getInstance(), "PlayBlock");
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        manager.removeViewer(event.getPlayer());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom().clone();
        Location to = event.getTo().clone();

        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ()) {
            manager.check(player);
        }
    }

    public static class PlayBlockCommands {
        private PlayBlock component;

        public PlayBlockCommands(PlayBlock component) {
            this.component = component;
        }

        @Command(aliases = {"pbplay"}, min = 0, max = 1, desc = "Play a URI")
        @CommandPermissions("skcraft.playblock.play")
        public void play(CommandContext context, CommandSender sender) throws CommandException {
            String uri = context.argsLength() > 0 ? context.getString(0) : null;
            component.manager.setMedia(uri);
            ChatUtil.msg(sender, ChatColor.YELLOW, "Media set to " + uri);
        }

        @Command(aliases = {"pbsetscreen"}, min = 2, max = 2, desc = "Play a URI")
        @CommandPermissions("skcraft.playblock.screen")
        public void setScreen(CommandContext context, CommandSender sender) throws CommandException {
            Player player = CommandUtil.checkPlayer(sender);
            int playRadius = context.getInteger(0);
            int stopRadius = context.getInteger(1);
            component.manager.setLocation(player.getLocation(), playRadius, stopRadius);
            ChatUtil.msg(sender, ChatColor.YELLOW, "New location set for screen!");
        }
    }
}
