/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

public class AnonymousZoneManager<T> extends MemoryZoneManager<T, SpatialIndex<T>> {

    @Override
    protected RTreeIndex<T> createSpatialIndex() {
        return new RTreeIndex<T>();
    }

}
