/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Location;

public class LocationUtil {

    private LocationUtil() {
    }

    public static boolean isDifferentBlock(Location from, Location to) {
        return from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ();
    }

    public static boolean isXZSquareDistAway(Location a, Location b, int dist) {
        int deltaX = a.getBlockX() - b.getBlockX();
        int deltaZ = a.getBlockZ() - b.getBlockZ();
        return deltaX > dist || -deltaX > dist || deltaZ > dist
                || -deltaZ > dist;
    }
    
    public static Location centerOf(Location location) {
        return location.add(0.5, 0, 0.5);
    }

}
