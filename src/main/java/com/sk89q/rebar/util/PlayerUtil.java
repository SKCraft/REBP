/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import static com.sk89q.rebar.util.BlockUtil.*;
import static com.sk89q.rebar.util.LocationUtil.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.rebar.Rebar;

public class PlayerUtil {

    private PlayerUtil() {
    }

    public static void safeTeleport(final Player player, final Location loc) {
        loc.getChunk().load(true);
        player.teleport(loc);

        Rebar.getInstance().registerTimeout(new Runnable() {
            @Override
            public void run() {
                player.teleport(loc);
            }
        }, 0);
    }

    public static boolean isSafeLandingSpot(Block block) {
        return BlockMaterialUtil.canPassThrough(block.getTypeId()) &&
                BlockMaterialUtil.canPassThrough(block.getRelative(0, 1, 0).getTypeId()) &&
                !BlockMaterialUtil.canPassThrough(block.getRelative(0, -1, 0).getTypeId());
    }

    public static Location findAdjacentFreePosition(Location pos) {
        Block block = pos.getBlock().getRelative(1, 0, 0);
        forceChunkLoad(block);
        if (isSafeLandingSpot(block))
            return centerOf(block.getLocation());

        block = pos.getBlock().getRelative(-1, 0, 0);
        forceChunkLoad(block);
        if (isSafeLandingSpot(block))
            return centerOf(block.getLocation());

        block = pos.getBlock().getRelative(0, 0, 1);
        forceChunkLoad(block);
        if (isSafeLandingSpot(block))
            return centerOf(block.getLocation());

        block = pos.getBlock().getRelative(0, 0, 1);
        forceChunkLoad(block);
        if (isSafeLandingSpot(block))
            return centerOf(block.getLocation());

        return findFreePosition(pos);

    }

    public static Location findFreePosition(Location pos) {
        World world = pos.getWorld();

        // Let's try going down
        Block block = pos.getBlock().getRelative(0, 1, 0);
        forceChunkLoad(block);
        int free = 0;

        // Look for ground
        while (block.getY() > 0 && BlockMaterialUtil.canPassThrough(block.getTypeId())) {
            free++;
            block = block.getRelative(0, -1, 0);
        }

        if (block.getY() == 0) return null; // No ground below!

        if (free >= 2) {
            if (BlockMaterialUtil.isDangerous(block.getType())) {
                return null; // Not safe
            }

            return centerOf(block.getRelative(0, 1, 0).getLocation());
        }

        // Let's try going up
        block = pos.getBlock().getRelative(0, -1, 0);
        free = 0;
        boolean foundGround = false;

        while (block.getY() <= world.getMaxHeight() + 1) {
            if (BlockMaterialUtil.canPassThrough(block.getTypeId())) {
                free++;
            } else {
                free = 0;
                foundGround = !BlockMaterialUtil.isDangerous(block.getType());
            }

            if (foundGround && free == 2) {
                return centerOf(block.getRelative(0, -1, 0).getLocation());
            }

            block = block.getRelative(0, 1, 0);
        }

        return null;
    }

}
