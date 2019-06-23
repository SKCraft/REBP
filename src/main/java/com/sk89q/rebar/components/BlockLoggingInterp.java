/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class BlockLoggingInterp extends AbstractComponent {

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    public void logBlockPlace(Player player, Block block, BlockState prevState) {
        BlockPlaceEvent event = new BlockPlaceEvent(block, prevState,
                block.getRelative(0, -1, 0), new ItemStack(block.getType(), 1), player, true);
        Rebar.server().getPluginManager().callEvent(event);
    }

}
