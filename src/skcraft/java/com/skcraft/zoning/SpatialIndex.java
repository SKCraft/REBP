/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.Region;

import java.util.Collection;
import java.util.List;

/**
 * A spatial index that manages the fast retrieval of geometry.
 *
 * @param <T> the data value
 */
public interface SpatialIndex<T> {

    /**
     * Add the given zone to the spatial index.
     *
     * @param zone the zone
     */
    void add(Zone<T> zone);

    /**
     * Remove the given zone from the spatial index.
     *
     * @param zone the zone
     */
    void remove(Zone<T> zone);

    /**
     * Get a list of zones that contain the given point.
     *
     * @param vec the point
     * @return a list of zones
     */
    List<Zone<T>> findContaining(Vector vec);

    /**
     * Get a list of zones that intersect with the given zone.
     *
     * @param region the zone to test against
     * @return a list of zones
     */
    List<Zone<T>> findIntersecting(Region region);

    /**
     * Add all the zones index to the given list.
     *
     * @param zones the list of zones to update
     */
    void list(List<Zone<T>> zones);

    /**
     * Get a collection of zones in the index.
     *
     * @return a collection of zones
     */
    Collection<Zone<T>> getZones();

}
