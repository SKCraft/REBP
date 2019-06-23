/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.network;

import java.util.*;

/**
 * Stores profiling data for a user by storing packet counts indexed by a given ID
 * string, such as that of a plugin or mod channel.
 */
public class ProfileData {

    private Map<String, UsageCount> counts = new HashMap<String, UsageCount>();
    private boolean intervalPrinting = false;
    private long startTime = System.currentTimeMillis();
    private boolean droppingCustom = false;

    /**
     * Get whether the results should be printed occasionally.
     *
     * @return true if printing periodically
     */
    public boolean isIntervalPrinting() {
        return intervalPrinting;
    }

    /**
     * Set whether the results should be printed occasionally.
     *
     * @param intervalPrinting true if printing periodically
     */
    public void setIntervalPrinting(boolean intervalPrinting) {
        this.intervalPrinting = intervalPrinting;
    }

    /**
     * Get whether custom packets should all be dropped.
     *
     * @return true if dropping custom packets
     */
    public boolean isDroppingCustom() {
        return droppingCustom;
    }

    /**
     * Set whether custom packets should all be dropped.
     *
     * @param droppingCustom true if dropping custom packets
     */
    public void setDroppingCustom(boolean droppingCustom) {
        this.droppingCustom = droppingCustom;
    }

    /**
     * Track a usage data point.
     *
     * @param id the type of data being tracked
     * @param bytes the number of bytes used
     */
    public synchronized void add(String id, int bytes) {
        UsageCount count = counts.get(id);

        // Create a new UsageCount if one isn't available
        if (count == null) {
            count = new UsageCount(id);
            counts.put(id, count);
        }

        count.add(bytes);
    }

    /**
     * Forget all logged data.
     */
    public synchronized void clear() {
        counts.clear();
        startTime = System.currentTimeMillis();
    }

    /**
     * Get the results without clearing the data.
     */
    public synchronized Result getResults() {
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;

        List<UsageCount> usages = new ArrayList<UsageCount>();

        for (UsageCount count : counts.values()) {
            usages.add(new UsageCount(count));
        }

        return new Result(usages, elapsed);
    }

    /**
     * Get the results and clearing the data.
     */
    public synchronized Result takeResults() {
        long now = System.currentTimeMillis();
        long elapsed = now - startTime;
        startTime = now;

        Map<String, UsageCount> counts = this.counts;
        this.counts = new HashMap<String, UsageCount>();

        return new Result(new ArrayList<UsageCount>(counts.values()), elapsed);
    }

    public static class Result {
        private final List<UsageCount> results;
        private final long elapsedTime;

        private Result(List<UsageCount> results, long elapsedTime) {
            this.results = results;
            this.elapsedTime = elapsedTime;
        }

        public List<UsageCount> getResults() {
            return results;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }
    }

}
