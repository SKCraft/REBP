/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components.sessions;

import org.bukkit.entity.Player;

public interface ProfileFactory<T> {

    public Class<T> getProfileClass();
    public boolean shouldPersist();
    public T create(Player player);
    
}
