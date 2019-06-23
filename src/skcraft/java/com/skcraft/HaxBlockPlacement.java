/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.material.Ladder;
import org.bukkit.material.MaterialData;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class HaxBlockPlacement extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @Override
    public void shutdown() {
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onBlockPhysics(BlockPhysicsEvent event) {
            Block block = event.getBlock();
            if (block.getType() == Material.TRAP_DOOR
                    || block.getType() == Material.WOOD_DOOR) {
                event.setCancelled(true);
                return;
            } else if (block.getType() == Material.LADDER) {
                MaterialData data = block.getState().getData();
                if (data instanceof Ladder) {
                    Block behind = block.getRelative(((Ladder) data).getAttachedFace());
                    if (behind.getTypeId() == 0 || !behind.getType().isBlock()) {
                        if (block.getRelative(0, 1, 0).getType() == Material.LADDER) {
                            event.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }

}
