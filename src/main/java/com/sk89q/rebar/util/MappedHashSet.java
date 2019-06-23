/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MappedHashSet<K, V> {

    private final Map<K, Set<V>> map = new HashMap<>();

    public boolean add(K key, V item) {
        Set<V> set;
        if ((set = map.get(key)) == null) {
            map.put(key, set = new HashSet<V>());
        }

        return set.add(item);
    }

    public boolean remove(K key, V item) {
        Set<V> collection;
        if ((collection = map.get(key)) != null) {
            boolean ret;
            ret = collection.remove(item);

            if (collection.size() == 0) {
               map.remove(key);
            }

            return ret;
        } else {
            return false;
        }
    }

    public void clear() {
        map.clear();
    }

}
