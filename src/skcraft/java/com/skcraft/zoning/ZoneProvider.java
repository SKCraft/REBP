/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import java.io.IOException;
import java.util.List;

/**
 * Provides the reading from and writing to of zone data.
 */
public interface ZoneProvider {

    /**
     * Read zone data from disk and add the zones to the index.
     *
     * @param index the index
     * @param <T> type of the extra zone data
     * @return the number of zones read
     * @throws IOException thrown on I/O error
     */
    <T> int read(SpatialIndex<T> index) throws IOException;

    /**
     * Write the list of zones to disk.
     *
     * @param zones the list of zones
     * @param <T> type of the extra zone data
     * @throws IOException thrown on I/O error
     */
    <T> void write(List<Zone<T>> zones) throws IOException;
}
