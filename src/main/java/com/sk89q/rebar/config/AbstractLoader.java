/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

public abstract class AbstractLoader<V> implements Loader<V> {

    @Override
    public V read(Object value) {
        return read(new ConfigurationValue(value));
    }
    
    public abstract V read(ConfigurationValue value);

}
