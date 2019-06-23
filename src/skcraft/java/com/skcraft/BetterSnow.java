/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.material.Door;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.BlockUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.bukkit.BukkitUtil;

@SuppressWarnings("deprecation")
public class BetterSnow extends AbstractComponent {

    private Set<BlockVector> visited = new HashSet<BlockVector>();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @Override
    public void shutdown() {
    }

    private boolean isOpen(Block block) {
        Material mat = block.getType();
        return (BlockType.canPassThrough(block.getTypeId())
                && !BlockType.canPassThrough(block.getRelative(0, -1, 0).getTypeId()))
                || mat == Material.LONG_GRASS
                || mat == Material.DEAD_BUSH
                || mat == Material.RED_ROSE
                || mat == Material.YELLOW_FLOWER
                || mat == Material.BROWN_MUSHROOM
                || mat == Material.RED_MUSHROOM
                || mat == Material.TORCH
                || mat == Material.WHEAT
                || mat == Material.CACTUS
                || mat == Material.SUGAR_CANE_BLOCK
                || mat == Material.PUMPKIN_STEM
                || mat == Material.MELON_STEM
                || mat == Material.VINE
                || mat == Material.SNOW
                || mat == Material.FENCE
                || mat == Material.FENCE_GATE
                || (mat == Material.WOODEN_DOOR && BlockUtil.getMaterialData(block, Door.class).isOpen())
                || (mat == Material.IRON_DOOR && BlockUtil.getMaterialData(block, Door.class).isOpen());
    }

    private boolean canBeUnderSnow(Block block) {
        Material mat = block.getType();
        return !BlockType.canPassThrough(block.getTypeId()) &&
                mat != Material.FENCE &&
                mat != Material.FENCE_GATE &&
                mat != Material.WOOD_STAIRS &&
                mat != Material.SPRUCE_WOOD_STAIRS &&
                mat != Material.BIRCH_WOOD_STAIRS &&
                mat != Material.JUNGLE_WOOD_STAIRS &&
                mat != Material.PISTON_EXTENSION &&
                mat != Material.PISTON_MOVING_PIECE &&
                mat != Material.STEP &&
                mat != Material.WOODEN_DOOR &&
                mat != Material.IRON_DOOR_BLOCK &&
                mat != Material.COBBLESTONE_STAIRS &&
                mat != Material.SUGAR_CANE_BLOCK &&
                mat != Material.CAKE_BLOCK &&
                mat != Material.CHEST &&
                mat != Material.TRAP_DOOR &&
                mat != Material.IRON_FENCE &&
                mat != Material.THIN_GLASS &&
                mat != Material.BRICK_STAIRS &&
                mat != Material.SMOOTH_STAIRS &&
                mat != Material.NETHER_FENCE &&
                mat != Material.NETHER_BRICK_STAIRS &&
                mat != Material.BREWING_STAND &&
                mat != Material.CAULDRON &&
                mat != Material.ENDER_PORTAL &&
                mat != Material.ENDER_PORTAL_FRAME &&
                mat != Material.DRAGON_EGG &&
                mat != Material.WOOD_STEP &&
                mat != Material.COCOA &&
                mat != Material.SANDSTONE_STAIRS &&
                mat != Material.TRIPWIRE &&
                mat != Material.TRIPWIRE_HOOK;
    }

    private boolean canReplaceAsSnow(Block block) {
        Material mat = block.getType();
        return (mat == Material.AIR
                || mat == Material.LONG_GRASS
                || mat == Material.DEAD_BUSH
                || mat == Material.RED_ROSE
                || mat == Material.YELLOW_FLOWER
                || mat == Material.BROWN_MUSHROOM
                || mat == Material.RED_MUSHROOM
                || mat == Material.TORCH
                || mat == Material.WHEAT
                || mat == Material.CACTUS
                || mat == Material.SUGAR_CANE_BLOCK
                || mat == Material.PUMPKIN_STEM
                || mat == Material.MELON_STEM
                || mat == Material.VINE) &&
                canBeUnderSnow(block.getRelative(0, -1, 0));
    }

    private void placeSnow(Block block, Block origin) {
        int y = origin.getY() - block.getY();
        double dist = block.getLocation().distanceSquared(origin.getLocation());
        if (y >= 4 || y <= -4) return;
        if (dist > 6 * 6) return;
        if (!isOpen(block)) return;
        BlockVector v = BukkitUtil.toVector(block).toBlockVector();
        if (visited.contains(v)) return;
        visited.add(v);

        if (canReplaceAsSnow(block) /*&& (block.getType() != Material.AIR || dist < 6)*/) {
            block.setType(Material.SNOW);
        }

        placeSnow(block.getRelative(1, 0, 0), origin);
        placeSnow(block.getRelative(-1, 0, 0), origin);
        placeSnow(block.getRelative(0, 0, 1), origin);
        placeSnow(block.getRelative(0, 0, -1), origin);
        placeSnow(block.getRelative(0, 1, 0), origin);
        placeSnow(block.getRelative(0, -1, 0), origin);
    }

    public class BlockListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onBlockForm(BlockFormEvent event) {
            if (event.isCancelled()) return;

            Block block = event.getBlock();
            if (event.getNewState().getType() != Material.SNOW) return;

            visited.clear();
            placeSnow(block, block);
        }
    }

}
