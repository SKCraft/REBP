/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A spatial index derived from {@link com.skcraft.zoning.RTreeIndex} that maintains
 * a mapping of case-insensitive zone names.
 *
 * @param <T> type of the extra zone data
 */
public class RTreeNamedIndex<T extends NamedData> extends RTreeIndex<T> implements NamedIndex<T> {

    private final Map<String, Zone<T>> names = new HashMap<String, Zone<T>>();

    @Override
    public void add(@NonNull Zone<T> zone) {
        String name = zone.getData().getName();
        String testName = name.toLowerCase();
        if (names.containsKey(testName)) {
            throw new IllegalArgumentException("There is already a zone with the given name");
        }
        super.add(zone);
        names.put(testName, zone);
    }

    @Override
    public void remove(@NonNull Zone<T> zone) {
        String name = zone.getData().getName();
        String testName = name.toLowerCase();
        super.remove(zone);
        names.remove(testName);
    }

    @Override
    public Zone<T> findByName(String name) {
        String testName = name.toLowerCase();
        return names.get(testName);
    }

}
