/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components.sessions;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

class Session {
    private Map<Class<?>, Object> sessionProfiles = new HashMap<Class<?>, Object>();
    private Map<Class<?>, Object> persistentProfiles = new HashMap<Class<?>, Object>();
    private long lastUpdate;
    
    public <T> T get(Player player, ProfileFactory<T> factory) {
        if (factory.shouldPersist()) {
            return get(persistentProfiles, player, factory);
        } else {
            return get(sessionProfiles, player, factory);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <T> T get(Map<Class<?>, Object> profiles, Player player, ProfileFactory<T> factory) {
        Class<T> cls = factory.getProfileClass();
        Object obj = profiles.get(cls);
        if (obj == null) {
            obj = factory.create(player);
            profiles.put(cls, obj);
        }
        return (T) obj;
    }

    public void markExpiration() {
        sessionProfiles.clear();
        lastUpdate = System.currentTimeMillis();
    }
    
    public boolean hasExpired() {
        return System.currentTimeMillis() - lastUpdate > 1000 * 30;
    }

    public void destroyPersistent() {
        for (Object obj : persistentProfiles.values()) {
            if (obj instanceof Managed) {
                ((Managed) obj).destroy();
            }
        }
        persistentProfiles.clear();
    }
}