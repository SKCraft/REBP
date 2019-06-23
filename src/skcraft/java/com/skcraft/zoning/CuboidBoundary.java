/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import lombok.NonNull;

/**
 * An axis-aligned cuboid.
 */
@JsonIgnoreProperties
@JsonAutoDetect(
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        fieldVisibility = JsonAutoDetect.Visibility.NONE)
public final class CuboidBoundary extends CuboidRegion implements Boundary {

    /**
     * Create a new instance.
     *
     * @param pos1 position 1
     * @param pos2 position 2
     */
    @JsonCreator
    public CuboidBoundary(@JsonProperty("p1") @NonNull Vector pos1, @JsonProperty("p2") @NonNull Vector pos2) {
        super(pos1, pos2);
    }

    @JsonProperty("p1")
    Vector getPoint1() {
        return getMinimumPoint();
    }

    @JsonProperty("p2")
    Vector getPoint2() {
        return getMaximumPoint();
    }

    @Override
    public boolean intersects(@NonNull Region region) {
        Vector rMaxPoint = region.getMaximumPoint();
        Vector min = getMinimumPoint();

        if (rMaxPoint.getBlockX() < min.getBlockX()) return false;
        if (rMaxPoint.getBlockY() < min.getBlockY()) return false;
        if (rMaxPoint.getBlockZ() < min.getBlockZ()) return false;

        Vector rMinPoint = region.getMinimumPoint();
        Vector max = getMaximumPoint();

        if (rMinPoint.getBlockX() > max.getBlockX()) return false;
        if (rMinPoint.getBlockY() > max.getBlockY()) return false;
        if (rMinPoint.getBlockZ() > max.getBlockZ()) return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CuboidBoundary that = (CuboidBoundary) o;

        Vector pos1 = getMinimumPoint();
        Vector pos2 = getMaximumPoint();
        if (!pos1.equals(that.getMinimumPoint())) return false;
        if (!pos2.equals(that.getMaximumPoint())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        Vector pos1 = getMinimumPoint();
        Vector pos2 = getMaximumPoint();
        int result = pos1.hashCode();
        result = 31 * result + pos2.hashCode();
        return result;
    }

    public static CuboidBoundary cuboidRegion(Region region) {
        return new CuboidBoundary(region.getMinimumPoint(), region.getMaximumPoint());
    }
}
