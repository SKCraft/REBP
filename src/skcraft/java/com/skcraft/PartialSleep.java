/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

public class PartialSleep extends AbstractComponent implements Listener {
    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        int sleeping = 0;
        int total = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isSleeping()) {
                sleeping++;
                total++;
            } else if (!player.isSleepingIgnored()) {
                total++;
            }
        }

        if (sleeping / (double) total >= 0.5) {
            Bukkit.getServer().getWorlds().get(0).setTime(0);
            Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "More than 50% of people slept.");
        }
    }
}
