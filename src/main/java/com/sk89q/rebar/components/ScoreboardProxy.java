/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Manages multiple scoreboards so that only the highest priority one is shown
 * at any given time.
 */
public class ScoreboardProxy extends AbstractComponent implements Listener {

    private final ConcurrentMap<String, SortedSet<ScoreboardEntry>> cache = new ConcurrentHashMap<>();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
        cache.clear();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        cache.remove(player.getName());
    }

    /**
     * Unset a scoreboard.
     *
     * @param player the player
     * @param owner the owner of the scoreboard
     * @return the scoreboard that is displayed
     */
    public Scoreboard unset(Player player, Object owner) {
        return set(player, null, owner, 0);
    }

    /**
     * Set a scoreboard for a player.
     *
     * @param player the player
     * @param scoreboard the scoreboard
     * @param owner the owner object
     * @param priority the piority, with higher numbers implying higher priority
     * @return the scoreboard that is displayed
     */
    public Scoreboard set(final Player player, Scoreboard scoreboard, Object owner, int priority) {
        if (player == null) {
            return null;
        }

        String key = player.getName();
        SortedSet<ScoreboardEntry> set;

        if (!cache.containsKey(key)) {
            // Removing the scoreboard, so just return now
            if (scoreboard == null) {
                return null;
            }

            cache.put(key, set = new TreeSet<ScoreboardEntry>());
        } else {
            set = cache.get(key);
        }

        set.remove(new ScoreboardEntry(owner, null, 0));
        if (scoreboard != null) {
            set.add(new ScoreboardEntry(owner, scoreboard, priority));
        }

        ScoreboardEntry top = set.size() > 0 ? set.first() : null;
        final Scoreboard winner;

        if (top != null) {
            winner = top.getScoreboard();
        } else {
            winner = null;
        }

        Rebar.getInstance().registerTimeout(new Runnable() {
            @Override
            public void run() {
                player.setScoreboard(winner != null ? winner :
                        Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }, 0);

        return winner;
    }

    private static class ScoreboardEntry implements Comparable<ScoreboardEntry> {
        private final Object owner;
        private final Scoreboard scoreboard;
        private final int priority;

        private ScoreboardEntry(Object owner, Scoreboard scoreboard, int priority) {
            this.owner = owner;
            this.scoreboard = scoreboard;
            this.priority = priority;
        }

        public Object getOwner() {
            return owner;
        }

        public int getPriority() {
            return priority;
        }

        public Scoreboard getScoreboard() {
            return scoreboard;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ScoreboardEntry) {
                return getOwner().equals(((ScoreboardEntry) obj).getOwner());
            }

            return false;
        }

        @Override
        public int hashCode() {
            return getOwner().hashCode();
        }

        @Override
        public int compareTo(ScoreboardEntry entry) {
            if (entry.equals(this)) {
                return 0;
            }

            if (getPriority() > entry.getPriority()) {
                return -1;
            } else if (getPriority() < entry.getPriority()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
