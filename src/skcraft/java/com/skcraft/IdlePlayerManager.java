/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.sk89q.rebar.Rebar;

public class IdlePlayerManager implements Runnable {

    private static final Logger logger = Logger.getLogger(IdlePlayerManager.class.getCanonicalName());
    private static final int IDLE_TIME = 1000 * 60;

    private Map<String, State> players = new LinkedHashMap<String, State>();

    public void update(Player player) {
        String name = player.getName();
        State state = players.get(name);
        if (state == null) {
            state = new State();
            players.put(name, state);
        } else {
            if (state.isIdle) {
                state.isIdle = false;
                onReturn(player);
            }
        }
        state.lastAction = System.currentTimeMillis();
    }

    public boolean isIdle(Player player) {
        String name = player.getName();
        State state = players.get(name);
        if (state == null) {
            return false;
        } else {
            return state.isIdle;
        }
    }

    public void forget(Player player) {
        players.remove(player.getName());
    }

    public void checkIdle() {
        long now = System.currentTimeMillis();

        Iterator<Map.Entry<String, State>> it = players.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, State> entry = it.next();
            State state = entry.getValue();
            if (!state.isIdle && now - state.lastAction > IDLE_TIME) {
                state.isIdle = true;
                Player player = Rebar.getInstance().getServer().getPlayer(entry.getKey());
                if (player != null) {
                    onIdle(player);
                } else {
                    it.remove();
                }
            }
        }
    }

    public void onReturn(Player player) {
        logger.info(player.getName() + " is now back");
        player.setSleepingIgnored(false);
        player.setPlayerListName(player.getName());
    }

    public void onIdle(Player player) {
        logger.info(player.getName() + " is now idle");
        player.setSleepingIgnored(true);
        player.setPlayerListName(getPlayerListName(player.getName()));
    }

    private static class State {
        public long lastAction;
        public boolean isIdle = false;
    }

    @Override
    public void run() {
        checkIdle();
    }

    public static String getPlayerListName(String name) {
        if (name.length() <= 14) {
            return ChatColor.DARK_GRAY + name;
        } else {
            return ChatColor.DARK_GRAY + name.substring(0, 14);
        }
    }
}
