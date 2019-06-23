/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * A type of geometry with associated data that can be added to a
 * {@link MemoryZoneManager}.
 *
 * @param <T> type of the extra zone data
 */
public class Zone<T> {

    @Getter private final Boundary boundary;
    @Getter @Setter private T data;

    /**
     * Create a new zone.
     *
     * @param boundary a boundary
     */
    @JsonCreator
    public Zone(@JsonProperty("boundary") @NonNull Boundary boundary) {
        this.boundary = boundary;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zone zone = (Zone) o;

        if (!boundary.equals(zone.boundary)) return false;
        if (data != null ? !data.equals(zone.data) : zone.data != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = boundary.hashCode();
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }

}
