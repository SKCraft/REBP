/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.zoning;

import com.google.common.base.Predicate;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of a {@link com.skcraft.zoning.ZoneManager} that keeps an
 * index of all the stored regions in memory.
 *
 * @param <T> type of the extra zone data
 */
public abstract class MemoryZoneManager<T, E extends SpatialIndex<T>> implements ZoneManager<T> {

    private final ListeningExecutorService providerExecutor =
            MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());

    @Getter @Setter private ZoneProvider provider;
    @Getter(AccessLevel.PROTECTED) private volatile E spatialIndex = createSpatialIndex();

    @Getter private final ReentrantLock lock = new ReentrantLock();
    private final Object providerObject = new Object();
    private volatile ListenableFuture<Integer> loadFuture;
    private volatile ListenableFuture<Integer> saveFuture;

    /**
     * Create a new spatial index.
     *
     * @return a new spatial index
     */
    protected abstract E createSpatialIndex();

    @Override
    public ListenableFuture<Integer> load(@NonNull List<Vector2D> chunks) {
        ZoneProvider provider = this.provider;
        checkNotNull(provider, "No region provider defined on this region manager");
        synchronized (providerObject) {
            if (loadFuture != null) {
                return loadFuture;
            }
            return loadFuture = providerExecutor.submit(new CompleteLoad(provider));
        }
    }

    @Override
    public ListenableFuture<Integer> save(List<Zone<T>> changed) {
        ZoneProvider provider = this.provider;
        checkNotNull(provider, "No region provider defined on this region manager");
        synchronized (providerObject) {
            if (saveFuture != null) {
                return saveFuture;
            }
            return saveFuture = providerExecutor.submit(new CompleteSave(provider));
        }
    }

    @Override
    public void cache(Vector2D chunk) {
    }

    @Override
    public void invalidate(Vector2D chunk) {
    }

    /**
     * Create a new zone with the given boundary.
     *
     * @param boundary a boundary
     * @param data the data attached with the zone, or null
     * @return the created zone
     */
    public Zone<T> create(@NonNull Boundary boundary, T data) {
        lock.lock();
        try {
            SpatialIndex<T> index = this.spatialIndex;
            Zone<T> zone = new Zone<T>(boundary);
            zone.setData(data);
            index.add(zone);
            return zone;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Remove a zone from the list.
     *
     * @param zone the zone
     */
    public void remove(@NonNull Zone<T> zone) {
        lock.lock();
        try {
            SpatialIndex<T> index = this.spatialIndex;
            index.remove(zone);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Collection<Zone<T>> getZones() {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Lock not held by current thread");
        }

        return spatialIndex.getZones();
    }

    @Override
    public List<Zone<T>> findContaining(@NonNull Vector vec) {
        lock.lock();
        try {
            return spatialIndex.findContaining(vec);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Zone<T>> findIntersecting(@NonNull Region region) {
        lock.lock();
        try {
            return spatialIndex.findIntersecting(region);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void applyContaining(@NonNull Vector vec, @NonNull Predicate<Zone<T>> function) {
        lock.lock();
        try {
            boolean changed = false;
            for (Zone<T> zone : findContaining(vec)) {
                changed = changed || function.apply(zone);
            }
            if (changed) {
                save(Collections.<Zone<T>>emptyList());
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void applyIntersecting(@NonNull Region region, @NonNull Predicate<Zone<T>> function) {
        lock.lock();
        try {
            boolean changed = false;
            for (Zone<T> zone : findIntersecting(region)) {
                changed = changed || function.apply(zone);
            }
            if (changed) {
                save(Collections.<Zone<T>>emptyList());
            }
        } finally {
            lock.unlock();
        }

    }

    /**
     * Loads the entire list of zones from disk using the set zone provider.
     */
    private class CompleteLoad implements Callable<Integer> {
        private final ZoneProvider provider;

        private CompleteLoad(ZoneProvider provider) {
            this.provider = provider;
        }

        @Override
        public Integer call() throws IOException {
            synchronized (providerObject) {
                loadFuture = null;
                saveFuture = null;
            }

            E newIndex = createSpatialIndex();
            int loaded = provider.read(newIndex);
            // Lock here because create(), etc. calls could be lost
            lock.lock();
            try {
                MemoryZoneManager.this.spatialIndex = newIndex;
            } finally {
                lock.unlock();
            }
            return loaded;
        }
    }

    /**
     * Writes the entire list of zones to disk using the set zone provider.
     */
    private class CompleteSave implements Callable<Integer> {
        private final ZoneProvider provider;

        private CompleteSave(ZoneProvider provider) {
            this.provider = provider;
        }

        @Override
        public Integer call() throws IOException {
            synchronized (providerObject) {
                loadFuture = null;
                saveFuture = null;
            }

            List<Zone<T>> zones = new ArrayList<Zone<T>>();
            lock.lock();
            try {
                spatialIndex.list(zones);
            } finally {
                lock.unlock();
            }

            provider.write(zones);
            return zones.size();
        }
    }

}
