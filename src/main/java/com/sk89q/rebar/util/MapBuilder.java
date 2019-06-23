/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {
    
    private final Map<K, V> map = new HashMap<K, V>();
    
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }
    
    public Map<K, V> map() {
        return map;
    }
    
    public static class ObjectMapBuilder extends MapBuilder<Object, Object> {
    }

}
