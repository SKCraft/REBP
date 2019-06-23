/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.skcraft.util.BlockCoords;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TNTFishing extends AbstractComponent implements Listener {

    private static final Random random = new Random();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    private int getPoolSize(Block block) {
        block = block.getRelative(random.nextInt(10) - 5, 0, random.nextInt(10) - 5);
        return getPoolSize(block, block.getX(), block.getY(), block.getZ(), 0, new HashSet<BlockCoords>());
    }

    private int getPoolSize(Block block, int ox, int oy, int oz, int curSize, Set<BlockCoords> visited) {
        if (block.getType() != Material.WATER
                && block.getType() != Material.STATIONARY_WATER) {
            return 0;
        }

        BlockCoords coords = new BlockCoords(block);

        if (curSize > 500) return 0;
        if (visited.contains(coords)) return 0;
        if (Math.abs(ox - block.getX()) > 90) return 0;
        if (Math.abs(oz - block.getZ()) > 90) return 0;
        //if (Math.abs(oy - block.getY()) > 5) return 0;

        visited.add(coords);

        int size = 1;
        size += getPoolSize(block.getRelative(-5, 0, 0), ox, oy, oz, size, visited);
        size += getPoolSize(block.getRelative(5, 0, 0), ox, oy, oz, size, visited);
        size += getPoolSize(block.getRelative(0, 0, -5), ox, oy, oz, size, visited);
        size += getPoolSize(block.getRelative(0, 0, 5), ox, oy, oz, size, visited);
        /*size += getPoolSize(block.getRelative(0, -10, 0), ox, oy, oz, size, visited);
        size += getPoolSize(block.getRelative(0, 10, 0), ox, oy, oz, size, visited);*/

        return size;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;

        Location loc = event.getEntity().getLocation();
        Block explodeBlock = loc.getBlock();

        if (explodeBlock.getType() != Material.WATER
                && explodeBlock.getType() != Material.STATIONARY_WATER) return;

        int poolSize = getPoolSize(loc.getBlock());
        if (poolSize == 0) return;

        double probability = random.nextDouble() * Math.min(1, poolSize / 500.0) * 0.50;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    Block block = loc.getWorld().getBlockAt(
                            loc.getBlockX() + x, loc.getBlockY() + y, loc.getBlockZ() + z);
                    if (block.getType() != Material.WATER
                            && block.getType() != Material.STATIONARY_WATER) {
                        continue;
                    }

                    if (random.nextDouble() > probability) {
                        continue;
                    }

                    block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5),
                            new ItemStack(Material.RAW_FISH, 1));
                }
            }
        }
    }

}
