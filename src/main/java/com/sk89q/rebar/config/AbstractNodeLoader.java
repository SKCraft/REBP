/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.Map;

public abstract class AbstractNodeLoader<V> implements Loader<V> {

    @Override
    public V read(Object value) {
        if (value != null && value instanceof ConfigurationNode) {
            return read((ConfigurationNode) value);
        } else if (value != null && value instanceof Map) {
            return read(new ConfigurationNode(value));
        } else {
            return null;
        }
    }

    public abstract V read(ConfigurationNode node);

}
