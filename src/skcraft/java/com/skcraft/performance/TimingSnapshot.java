/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class TimingSnapshot {

    public static final int TICKS_IN_SECOND = 20;
    
    private final int[] timings;
    private final int[] intervals;

    public TimingSnapshot(int[] timings, int[] intervals) {
        this.timings = timings;
        this.intervals = intervals;
    }
    
    public long[] getMeasureIntervals(TimeUnit u) {
        return scale(Arrays.copyOf(intervals, intervals.length), TimeUnit.SECONDS, u);
    }
    
    public long[] getRawTimes(TimeUnit u) {
        return scale(Arrays.copyOf(timings, timings.length), TimeUnit.MILLISECONDS, u);
    }

    public long[] getActualSecondTimes(TimeUnit u) {
        long[] out = new long[timings.length];
        for (int i = 0; i < timings.length; i++) {
            out[i] = u.convert(
                    (long) ((timings[i] / (double) intervals[i]) * 1000000), TimeUnit.NANOSECONDS);
        }
        return out;
    }

    public long[] getTickTimes(TimeUnit u) {
        long[] out = new long[timings.length];
        for (int i = 0; i < timings.length; i++) {
            out[i] = u.convert(
                    (long) ((timings[i] / (double) intervals[i] / TICKS_IN_SECOND) * 1000000), TimeUnit.NANOSECONDS);
        }
        return out;
    }

    public double[] getTicksPerSecond() {
        double[] out = new double[timings.length];
        for (int i = 0; i < timings.length; i++) {
            out[i] = (TICKS_IN_SECOND * 1000) / (timings[i] / (double) intervals[i]);
        }
        return out;
    }

    private static long[] scale(int[] data, TimeUnit from, TimeUnit to) {
        long[] out = new long[data.length];
        for (int i = 0; i < data.length; i++) {
            out[i] = to.convert((long) data[i], from);
        }
        return out;
    }
    
}
