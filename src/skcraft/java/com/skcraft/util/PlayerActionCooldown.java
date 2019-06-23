/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerActionCooldown implements Listener {

    private Map<String, Long> lastUse = new HashMap<>();
    @Getter @Setter
    private long defaultDelay;

    public PlayerActionCooldown(long defaultDelay) {
        this.defaultDelay = defaultDelay;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastUse.remove(event.getPlayer().getName());
    }

    public Long getLastUse(Player player) {
        return lastUse.get(player.getName());
    }

    public boolean tryUse(Player player, long delay) {
        Long time = getLastUse(player);
        long now = System.currentTimeMillis();
        lastUse.put(player.getName(), now);
        return time == null || (now - time) > delay;
    }

    public boolean tryUse(Player player) {
        return tryUse(player, defaultDelay);
    }

}
