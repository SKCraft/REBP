/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AntiGlitch extends AbstractComponent implements Listener {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return; // Ignore creative mode
        }

        World world = player.getWorld();
        if (world.getEnvironment() == Environment.NETHER) {
            Location loc = event.getFrom();
            if (loc.getY() >= 127) {
                event.setTo(PlayerUtil.findFreePosition(world.getSpawnLocation()));
                ChatUtil.msg(player, ChatColor.YELLOW,
                        "Whoops! You appeared on top of Nether! You've been moved back to spawn.");
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        event.setMessage(ChatColor.stripColor(event.getMessage()).replaceAll("\\s{3,}", " "));
    }

}
