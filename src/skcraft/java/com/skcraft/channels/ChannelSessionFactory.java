/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import org.bukkit.entity.Player;

import com.sk89q.rebar.components.sessions.ProfileFactory;

public class ChannelSessionFactory implements ProfileFactory<ChannelSession> {
    
    public Class<ChannelSession> getProfileClass() {
        return ChannelSession.class; 
    }

    public boolean shouldPersist() {
        return true;
    }

    public ChannelSession create(Player player) {
        return new ChannelSession(player);
    }
    
}
