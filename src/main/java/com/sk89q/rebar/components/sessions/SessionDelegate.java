/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components.sessions;

import org.bukkit.entity.Player;

public class SessionDelegate<T> {
    private final Sessions sessions;
    private final ProfileFactory<T> factory;
    
    SessionDelegate(Sessions sessions, ProfileFactory<T> factory) {
        this.sessions = sessions;
        this.factory = factory;
    }
    
    public T get(Player player) {
        return sessions.get(player, factory);
    }
}