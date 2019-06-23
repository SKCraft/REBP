/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import com.sk89q.worldedit.blocks.BlockID;
import org.bukkit.BlockChangeDelegate;
import org.bukkit.World;

import java.util.Random;

public class GlowstoneTreeDelegate implements BlockChangeDelegate {

    private final Random random = new Random();
    private World world;

    public GlowstoneTreeDelegate(World world) {
        this.world = world;
    }

    @Override
    public boolean setRawTypeId(int x, int y, int z, int typeId) {
        return setRawTypeIdAndData(x, y, z, typeId, 0);
    }

    @Override
    public boolean setRawTypeIdAndData(int x, int y, int z, int typeId, int data) {
        switch (typeId) {
        case BlockID.LOG:
            typeId = BlockID.OBSIDIAN;
            break;
        case BlockID.LEAVES:
            typeId = BlockID.LIGHTSTONE;
            break;
        default:
            typeId = 0;
        }

       return world.getBlockAt(x, y, z).setTypeIdAndData(typeId, (byte) 0, true);
    }

    @Override
    public boolean setTypeId(int x, int y, int z, int typeId) {
        return setRawTypeId(x, y, z, typeId);
    }

    @Override
    public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data) {
        return setRawTypeIdAndData(x, y, z, typeId, data);
    }

    @Override
    public int getTypeId(int x, int y, int z) {
        return world.getBlockTypeIdAt(x, y, z);
    }

    @Override
    public int getHeight() {
        return world.getMaxHeight();
    }

    @Override
    public boolean isEmpty(int x, int y, int z) {
        return world.getBlockTypeIdAt(x, y, z) == 0;
    }

}
