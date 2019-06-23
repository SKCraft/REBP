/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.StructureGrowEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class ChildSafeTrees extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    private static class Listener implements org.bukkit.event.Listener {

        @EventHandler(ignoreCancelled = true)
        public void onStructureGrow(StructureGrowEvent event) {
            Iterator<BlockState> it = event.getBlocks().iterator();
            while (it.hasNext()) {
                BlockState next = it.next();
                Material type = next.getBlock().getType();
                if (type != Material.LEAVES && type != Material.AIR) {
                    it.remove();
                }
            }
        }

    }

}
