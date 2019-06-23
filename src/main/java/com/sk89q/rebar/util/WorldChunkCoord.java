/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Chunk;
import org.bukkit.World;

public class WorldChunkCoord extends ChunkCoord {

    private World world;

    public WorldChunkCoord(World world) {
        this.world = world;
    }

    public WorldChunkCoord(World world, int x, int z) {
        super(x, z);
        this.world = world;
    }

    public WorldChunkCoord(Chunk chunk) {
        super(chunk.getX(), chunk.getZ());
        this.world = chunk.getWorld();
    }

    public World getWorld() {
        return world;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldChunkCoord)) {
            return false;
        }

        WorldChunkCoord other = (WorldChunkCoord) obj;
        return this.world.equals(other.world) && getX() == other.getX() && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return getX() + getZ() << 16 + this.world.hashCode() << 8;
    }

    public Chunk getChunk() {
        return world.getChunkAt(getX(), getZ());
    }
}
