/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class BetterMobSpawning extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new EntityListener());
    }

    @Override
    public void shutdown() {
    }

    public boolean ignoreMaterial(Material mat) {
        return mat == Material.LEAVES ||
                mat == Material.WOOD ||
                mat == Material.DOUBLE_STEP ||
                mat == Material.SMOOTH_BRICK ||
                mat == Material.WOOD_STAIRS ||
                mat == Material.COBBLESTONE_STAIRS ||
                mat == Material.BRICK_STAIRS ||
                mat == Material.SMOOTH_STAIRS;
    }

    public boolean isTooBright(Block block) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -1; y <= 1; y++) {
                    if (block.getRelative(x, y, z).getLightLevel() >= 6) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public class EntityListener implements Listener {
        @EventHandler
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            Entity ent = event.getEntity();
            Location loc = ent.getLocation();
            SpawnReason reason = event.getSpawnReason();
            Block block = loc.getBlock();

            if (reason != SpawnReason.NATURAL) return;
            if (!(ent instanceof Monster)) return;

            if (ignoreMaterial(block.getRelative(0, -1, 0).getType())) {
                event.setCancelled(true);
                return;
            }

            if (!ent.getWorld().hasStorm() && isTooBright(block)) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
