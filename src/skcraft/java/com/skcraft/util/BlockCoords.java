/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.block.Block;

public class BlockCoords {
    private int x;
    private int y;
    private int z;

    public BlockCoords(Block block) {
        x = block.getX();
        y = block.getY();
        z = block.getZ();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BlockCoords)) {
            return false;
        }
        BlockCoords other = (BlockCoords)obj;
        return other.x == this.x && other.y == this.y
                && other.z == this.z;

    }

    @Override
    public int hashCode() {
        return (Integer.valueOf(x).hashCode() << 19) ^
                (Integer.valueOf(y).hashCode() << 12) ^
                 Integer.valueOf(z).hashCode();
    }
}
