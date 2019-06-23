/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockType;

public class BlockUtil {
    
    private BlockUtil() {
    }
    
    public static void forceChunkLoad(Block block) {
        if (!block.getChunk().isLoaded()) {
            block.getChunk().load();
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T getState(Block block, Class<T> expected) {
        BlockState state = block.getState();
        
        if (state != null && expected.isAssignableFrom(state.getClass())) {
            return (T) state;
        }
        
        throw new RuntimeException("Expected block state of " + expected.getCanonicalName()
                + " but instead got " + String.valueOf(state));
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends MaterialData> T getMaterialData(Block block, Class<T> expected) {
        MaterialData data = block.getState().getData();
        
        if (data != null && expected.isAssignableFrom(data.getClass())) {
            return (T) data;
        }
        
        throw new RuntimeException("Expected block material of " + expected.getCanonicalName()
                + " but instead got " + String.valueOf(data));
    }
    
    public static void drop(Block block) {
        BaseItemStack stack = BlockType.getBlockDrop(block.getTypeId(), block.getData());
        block.setTypeId(0);
        if (stack != null) {
            block.getWorld().dropItemNaturally(block.getLocation(),
                    new ItemStack(stack.getType(), stack.getAmount(), stack.getDamage()));
        }
    }
    
    public static void signPostFromWall(Block block) {
        if (block.getType() != Material.WALL_SIGN) {
            return;
        }
        
        Sign sign = getState(block, Sign.class);
        String[] oldLines = sign.getLines();
        
        signPostFromWall(block, oldLines);
    }
    
    public static void signPostFromWall(Block block, String[] oldLines) {
        if (block.getType() != Material.WALL_SIGN) {
            return;
        }
        
        byte newData = 0;
        
        switch (block.getData()) {
        case 0x2:
            newData = 0x8; break;
        case 0x3:
            newData = 0x0; break;
        case 0x4:
            newData = 0x4; break;
        case 0x5:
            newData = 0xC; break;
        }
        
        block.setType(Material.SIGN_POST);
        block.setData(newData);
        Sign sign = getState(block, Sign.class);
        for (int i = 0; i < oldLines.length; i++) {
            sign.setLine(i, oldLines[i]);
        }
        sign.update();
    }

    public static boolean isSign(Block block) {
        return block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN;
    }
    
    public static BlockFace getLeftOf(BlockFace face) {
        switch (face) {
            case EAST: return BlockFace.NORTH;
            case NORTH: return BlockFace.WEST;
            case WEST: return BlockFace.SOUTH;
            case SOUTH: return BlockFace.EAST;
            default: return null;
        }
    }
    
    public static BlockFace getRightOf(BlockFace face) {
        switch (face) {
            case EAST: return BlockFace.SOUTH;
            case NORTH: return BlockFace.EAST;
            case WEST: return BlockFace.NORTH;
            case SOUTH: return BlockFace.WEST;
            default: return null;
        }
    }
    
}
