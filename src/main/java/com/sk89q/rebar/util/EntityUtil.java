/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

public class EntityUtil {
    
    private EntityUtil() {
    }

    public static BlockFace getBlockFacing(Entity entity) {
        // From hey0's code
        double rot = (entity.getLocation().getYaw() - 90) % 360;
        if (rot < 0) {
            rot += 360.0;
        }
        
        if (0 <= rot && rot < 22.5) {
            return BlockFace.NORTH;
        } else if (22.5 <= rot && rot < 67.5) {
            return null;
        } else if (67.5 <= rot && rot < 112.5) {
            return BlockFace.EAST;
        } else if (112.5 <= rot && rot < 157.5) {
            return null;
        } else if (157.5 <= rot && rot < 202.5) {
            return BlockFace.SOUTH;
        } else if (202.5 <= rot && rot < 247.5) {
            return null;
        } else if (247.5 <= rot && rot < 292.5) {
            return BlockFace.WEST;
        } else if (292.5 <= rot && rot < 337.5) {
            return null;
        } else if (337.5 <= rot && rot < 360.0) {
            return BlockFace.NORTH;
        } else {
            return null;
        }
    }
    
}
