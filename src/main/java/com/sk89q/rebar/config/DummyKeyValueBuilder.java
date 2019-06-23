/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.Map.Entry;

public class DummyKeyValueBuilder<K, V> implements KeyValueLoader<K, V>, KeyValueBuilder<K, V> {

    private final KeyValueLoader<K, V> loader;

    public DummyKeyValueBuilder(KeyValueLoader<K, V> loader) {
        this.loader = loader;
    }

    @Override
    public Entry<Object, Object> write(K key, V value) {
        return null;
    }

    @Override
    public Entry<K, V> read(Object key, Object value) {
        return loader.read(key, value);
    }

}
