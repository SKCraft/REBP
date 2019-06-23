/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.Map;

public class KeyValue<K, V> implements Map.Entry<K, V> {

    private K key;
    private V value;
    
    public KeyValue(K key, V entry) {
        this.key = key;
        this.value = entry;
    }
    
    @Override
    public K getKey() {
        return key;
    }

    @Override
    public V getValue() {
        return value;
    }

    @Override
    public V setValue(V value) {
        V old = value;
        this.value = value;
        return old;
    }

}
