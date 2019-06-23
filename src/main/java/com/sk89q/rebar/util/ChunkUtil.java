/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkUtil {
    
    private ChunkUtil() {
    }
    
    public static Chunk getRelative(Chunk chunk, int deltaX, int deltaZ) {
        return chunk.getWorld().getChunkAt(chunk.getX() + deltaX, chunk.getZ() + deltaZ);
    }
    
    public static Chunk getPotentialChunk(World world, int x, int z) {
        if (world.isChunkLoaded(x, z)) {
            return world.getChunkAt(x, z);
        }
        
        return null;
    }
    
    public static Chunk getPotentialChunk(WorldChunkCoord coord) {
        World world = coord.getWorld();
        int x = coord.getX();
        int z = coord.getZ();
        if (world.isChunkLoaded(x, z)) {
            return world.getChunkAt(x, z);
        }
        
        return null;
    }

}
