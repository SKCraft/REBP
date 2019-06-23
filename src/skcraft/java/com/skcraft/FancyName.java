/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import java.util.Random;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;


public class FancyName extends AbstractComponent {
    private static Random random = new Random();

    public final static ChatColor[] NAME_COLORS = new ChatColor[] {
            ChatColor.GOLD, ChatColor.GRAY, ChatColor.BLUE, ChatColor.GREEN,
            ChatColor.AQUA, ChatColor.RED, ChatColor.LIGHT_PURPLE,
            ChatColor.DARK_AQUA, ChatColor.DARK_GREEN,
            ChatColor.DARK_PURPLE, ChatColor.DARK_RED, ChatColor.YELLOW };

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());

        for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
            fancifyPlayer(player);
        }
    }

    @Override
    public void shutdown() {
    }

    public void fancifyPlayer(Player player) {
        player.setDisplayName(NAME_COLORS[random.nextInt(NAME_COLORS.length)]
                + player.getName() + ChatColor.RESET);
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerJoin(PlayerJoinEvent event) {
            fancifyPlayer(event.getPlayer());
        }
    }
}
