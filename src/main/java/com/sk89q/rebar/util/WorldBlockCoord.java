/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.World;
import org.bukkit.block.Block;

public class WorldBlockCoord extends BlockCoord {

    private World world;

    public WorldBlockCoord(World world) {
        this.world = world;
    }

    public WorldBlockCoord(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = world;
    }

    public WorldBlockCoord(Block block) {
        super(block.getX(), block.getY(), block.getZ());
        this.world = block.getWorld();
    }

    public World getWorld() {
        return world;
    }

    public Block getBlock() {
        return world.getBlockAt(getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldBlockCoord)) {
            return false;
        }

        WorldBlockCoord other = (WorldBlockCoord) obj;
        return this.world.equals(other.world) && getX() == other.getX()
                && getY() == other.getY() && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return getX() + getY() << 8 + getZ() << 16 + this.world.hashCode() << 7;
    }
}
