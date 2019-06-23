/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Chunk;

public class ChunkCoord {

    private int x;
    private int z;

    public ChunkCoord() {
    }

    public ChunkCoord(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public ChunkCoord(Chunk chunk) {
        this(chunk.getX(), chunk.getZ());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ChunkCoord)) {
            return false;
        }

        ChunkCoord other = (ChunkCoord) obj;
        return this.x == other.x && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return this.x + this.z << 16;
    }

}