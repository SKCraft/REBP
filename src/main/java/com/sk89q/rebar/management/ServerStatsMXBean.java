/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.management;

import java.util.Map;

public interface ServerStatsMXBean {

    int getWorldCount();

    int getLoadedChunkCount();

    Map<String, Integer> getWeatherDurations();

    Map<String, Integer> getEntityCounts();

    Map<String, Integer> getThunderDurations();

    int getPlayerCount();

    int getEntityCount();

    int getLivingEntityCount();

    int getItemEntityCount();

}
