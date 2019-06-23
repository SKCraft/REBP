/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Manages the creation, removal, and indexing of {@link com.skcraft.zoning.Zone}s.
 * <p>
 * Some zone managers support the partial caching of only zones that intersect the
 * currently loaded chunks. These type of zone managers will not have the entire
 * list of zones stored in-memory
 *
 * @param <T> type of the extra zone data
 */
public interface ZoneManager<T> {

    /**
     * Get the lock that can be used to surround bulk operations upon the
     * zone manager.
     *
     * @return the lock
     */
    ReentrantLock getLock();

    /**
     * Load from a blank index the required zones for the given chunks into
     * memory and index them for fast retrieval.
     * <p>
     * The returned future will provide the number of zones that have been
     * loaded at the end of the operation.
     * <p>
     * For non-partial zone managers, this method may load the entire list of
     * zones into memory.
     *
     * @param chunks a list of chunk coordinates
     * @return a future for the loading task
     */
    ListenableFuture<Integer> load(List<Vector2D> chunks);

    /**
     * Save the given list of zones to disk.
     * <p>
     * For non-partial zone managers, this method may rewrite all zones back
     * to disk.
     *
     * @param changed a list of changed zones
     * @return a future returning the list of zones
     */
    ListenableFuture<Integer> save(List<Zone<T>> changed);

    /**
     * Load the required zones to cover the given chunk from disk and index them
     * for fast retrieval.
     * <p>
     * For non-partial zone managers, this method may do nothing.
     *
     * @param chunk the chunk coordinate
     */
    void cache(Vector2D chunk);

    /**
     * Invalidate and remove the zones from memory that are no longer
     * intersected by a loaded chunk after the given chunk has been unloaded.
     * <p>
     * For non-partial zone managers, this method may do nothing.
     *
     * @param chunk the chunk coordinate
     */
    void invalidate(Vector2D chunk);

    /**
     * Query the in-memory index for all zones.
     * <p>
     * For partial zone managers, this method may return only cached zones.
     * <p>
     * A lock must be held by the current thread before this method can be called.
     *
     * @return a list of zones
     * @throws java.lang.IllegalStateException if a lock is not held
     */
    Collection<Zone<T>> getZones() throws IllegalStateException;

    /**
     * Query the in-memory index for zones that contain the given location.
     * <p>
     * For partial zone managers, this method may return null if the data for
     * the given point is not yet available.
     *
     * @param vec the location
     * @return a list of zones, or null
     */
    List<Zone<T>> findContaining(Vector vec);

    /**
     * Query the in-memory index for zones that intersect the given region.
     * <p>
     * For partial zone managers, this method may return null if the data for
     * the given intersecting region not yet available.
     *
     * @param region the region
     * @return a list of zones, or null
     */
    List<Zone<T>> findIntersecting(Region region);

    /**
     * Perform a query and apply the given function onto all zones that
     * contain the given point, including zones that are not cached.
     * <p>
     * The passed function should return true if the zone that it has
     * received has been modified by the function. If true is returned,
     * then changes to the region will be saved.
     *
     * @param vec the point
     * @param function the function to apply to every zone
     */
    void applyContaining(Vector vec, Predicate<Zone<T>> function);

    /**
     * Perform a query and apply the given function onto all zones that
     * intersect the given region, including zones that are not cached.
     * <p>
     * The passed function should return true if the zone that it has
     * received has been modified by the function. If true is returned,
     * then changes to the region will be saved.
     *
     * @param region the region
     * @param function the function to apply to every zone
     */
    void applyIntersecting(Region region, Predicate<Zone<T>> function);
}
