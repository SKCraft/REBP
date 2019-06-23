/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

public class SimpleZoneManager<T extends NamedData> extends MemoryZoneManager<T, NamedIndex<T>> {

    /**
     * Get the zone with the given name (case insensitive).
     *
     * @param name the name
     * @return a zone, or null if not found
     */
    public Zone<T> findByName(String name) {
        try {
            getLock().lock();
            NamedIndex<T> index = getSpatialIndex();
            return index.findByName(name);
        } finally {
            getLock().unlock();
        }
    }

    @Override
    protected RTreeNamedIndex<T> createSpatialIndex() {
        return new RTreeNamedIndex<T>();
    }

}
