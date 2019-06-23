/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

public interface NamedIndex<T> extends SpatialIndex<T> {

    /**
     * Get the zone with the given name (case insensitive).
     *
     * @param name the name
     * @return a zone, or null if not found
     */
    Zone<T> findByName(String name);

}
