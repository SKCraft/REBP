/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components.sessions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class Sessions extends AbstractComponent {

    private Map<String, Session> sessions = new HashMap<String, Session>();
    Sessions self = this;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());
        Rebar.getInstance().registerThreadedInterval(new SessionChecker(), 20, 20 * 10);
    }

    @Override
    public void shutdown() {
    }

    public synchronized Session get(Player player) {
        Session sess = sessions.get(player.getName());
        if (sess == null) {
            sess = new Session();
            sessions.put(player.getName(), sess);
        }
        return sess;
    }

    public synchronized <T> T get(Player player, Class<T> profileClass) {
        return get(player).get(player, new DefaultProfileFactory<T>(profileClass));
    }

    public synchronized <T> T get(Player player, ProfileFactory<T> factory) {
        return get(player).get(player, factory);
    }

    public synchronized Session remove(Player player) {
        return sessions.remove(player.getName());
    }

    private synchronized void markExpiration(Player player) {
        Session sess = sessions.get(player.getName());
        if (sess != null) {
            sess.markExpiration();
        }
    }

    public synchronized void clear() {
        sessions.clear();
    }

    public <T> SessionDelegate<T> forProfile(ProfileFactory<T> factory) {
        return new SessionDelegate<T>(this, factory);
    }

    public <T> SessionDelegate<T> forProfile(Class<T> cls) {
        return new SessionDelegate<T>(this, new DefaultProfileFactory<T>(cls));
    }

    private class SessionChecker implements Runnable {
        @Override
        public void run() {
            Set<String> online = new HashSet<String>();
            for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
                online.add(player.getName());
            }

            synchronized (self) {
                Iterator<Entry<String, Session>> it = sessions.entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, Session> entry = it.next();
                    String name = entry.getKey();
                    if (!online.contains(name)) {
                        if (entry.getValue().hasExpired()) {
                            entry.getValue().destroyPersistent();
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            markExpiration(event.getPlayer());
        }
    }
}
