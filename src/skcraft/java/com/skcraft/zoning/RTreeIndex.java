/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.infomatiq.jsi.Rectangle;
import com.infomatiq.jsi.rtree.RTree;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntProcedure;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An implementation of a {@link SpatialIndex} utilizing an RTree.
 *
 * @param <T> type of the extra zone data
 */
public class RTreeIndex<T> implements SpatialIndex<T> {

    private int nextIndex = 0;
    @Getter private final TIntObjectMap<Zone<T>> boundaries = new TIntObjectHashMap<Zone<T>>();
    @Getter private final com.infomatiq.jsi.SpatialIndex index = new RTree();

    /**
     * Create a new index.
     */
    public RTreeIndex() {
        index.init(null);
    }

    @Override
    public void add(@NonNull Zone<T> zone) {
        int thisIndex = nextIndex++;
        boundaries.put(thisIndex, zone);
        Boundary geom = zone.getBoundary();
        index.add(createRectangle(geom), thisIndex);
    }

    @Override
    public void remove(@NonNull Zone<T> zone) {
        TIntObjectIterator<Zone<T>> it = boundaries.iterator();
        while (it.hasNext()) {
            it.advance();
            if (it.value().equals(zone)) {
                index.delete(createRectangle(it.value().getBoundary()), it.key());
                it.remove();
            }
        }
    }

    @Override
    public List<Zone<T>> findContaining(@NonNull final Vector vec) {
        return findIntersecting(new CuboidRegion(vec, vec));
    }

    @Override
    public List<Zone<T>> findIntersecting(@NonNull final Region region) {
        Rectangle rect = createRectangle(region);
        List<Zone<T>> results = new ArrayList<Zone<T>>();
        index.intersects(rect, new ResultsCollector(results) {
            @Override
            public boolean matches(Zone<T> zone) {
                return zone.getBoundary().intersects(region);
            }
        });
        return results;
    }

    @Override
    public void list(@NonNull List<Zone<T>> zones) {
        TIntObjectIterator<Zone<T>> it = boundaries.iterator();
        while (it.hasNext()) {
            it.advance();
            zones.add(it.value());
        }
    }

    @Override
    public Collection<Zone<T>> getZones() {
        return boundaries.valueCollection();
    }

    /**
     * Create a new rectangle for the given region.
     *
     * @param region the region
     * @return a rectangle
     */
    private Rectangle createRectangle(Region region) {
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        return new Rectangle(min.getBlockX(), min.getBlockY(), max.getBlockZ() + 1, max.getBlockZ() + 1);
    }

    /**
     * A procedure to add each zone to a list (matching a certain criteria
     * as defined in {@link #matches(Zone)}).
     */
    private abstract class ResultsCollector implements TIntProcedure {
        private final List<Zone<T>> results;

        private ResultsCollector(List<Zone<T>> results) {
            this.results = results;
        }

        @Override
        public boolean execute(int key) {
            Zone<T> zone = boundaries.get(key);
            if (matches(zone)) {
                results.add(zone);
            }
            return true;
        }

        public abstract boolean matches(Zone<T> zone);
    }

}
