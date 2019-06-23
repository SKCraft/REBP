/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Material;

import com.sk89q.worldedit.blocks.BlockType;

public class BlockMaterialUtil {
    
    private BlockMaterialUtil() {
    }
    
    public static boolean canPassThrough(int id) {
        return BlockType.canPassThrough(id);
    }

    public static boolean isRedstoneSourceBlock(Material type) {
        return type == Material.STONE_BUTTON ||
                type == Material.LEVER ||
                type == Material.DETECTOR_RAIL ||
                type == Material.WOOD_PLATE ||
                type == Material.STONE_PLATE ||
                type == Material.REDSTONE_TORCH_OFF ||
                type == Material.REDSTONE_TORCH_ON ||
                type == Material.DIODE_BLOCK_OFF ||
                type == Material.DIODE_BLOCK_ON ||
                type == Material.REDSTONE_WIRE;
    }
    
    public static boolean isDangerous(Material type) {
        return type == Material.LAVA ||
                type == Material.STATIONARY_LAVA ||
                        type == Material.FIRE;
    }

}
