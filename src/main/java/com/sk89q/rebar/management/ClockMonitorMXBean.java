/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.management;

public interface ClockMonitorMXBean {

    double[] getTicksPerSecond();

    long[] getTickTimes();

    double getTickRate();

}
