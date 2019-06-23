/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components.sessions;

import org.bukkit.entity.Player;

public class DefaultProfileFactory<T> implements ProfileFactory<T> {

    private Class<T> cls;

    public DefaultProfileFactory(Class<T> cls) {
        this.cls = cls;
    }

    @Override
    public Class<T> getProfileClass() {
        return cls;
    }

    @Override
    public boolean shouldPersist() {
        return false;
    }

    @Override
    public T create(Player player) {
        try {
            return cls.newInstance();
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        }
    }

}
