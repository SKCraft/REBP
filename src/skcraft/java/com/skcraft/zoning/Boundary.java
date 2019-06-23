/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sk89q.worldedit.regions.Region;

/**
 * A type of geometry that extends {@link com.sk89q.worldedit.regions.Region}
 * but is more appropriate for use with a {@link com.skcraft.zoning.Zone}.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(
        value = CuboidBoundary.class, name = "cuboid")
})
public interface Boundary extends Region {

    /**
     * Checks whether the given region intersects (overlaps) with this region.
     *
     * @param region the other region to test again
     * @return true if intersection occurs
     */
    boolean intersects(Region region);

}
