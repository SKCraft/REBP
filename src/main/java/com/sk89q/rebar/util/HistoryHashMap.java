/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar.util;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 *
 * @author sk89q
 * @param <K> 
 * @param <V> 
 */
public class HistoryHashMap<K,V> extends LinkedHashMap<K,V> {
    private static final long serialVersionUID = -3275917656900940011L;

    private int maxEntries;
    
    public HistoryHashMap(int maxEntries) {
        super();
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxEntries;
    }
}
