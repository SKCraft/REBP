/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.block.Block;

public class BlockCoord {

    private int x;
    private int y;
    private int z;

    public BlockCoord() {
    }

    public BlockCoord(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockCoord(Block block) {
        this(block.getX(), block.getY(), block.getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockCoord)) {
            return false;
        }

        BlockCoord other = (BlockCoord) obj;
        return this.x == other.x && this.y == other.y && this.z == other.z;
    }

    @Override
    public int hashCode() {
        return this.x + this.y << 8 + this.z << 16;
    }

}