/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.event.AbstractEvent;

public class BlockInteractEvent extends AbstractEvent<ProtectionListener> {

    private final Block block;
    private final Entity causer;
    private final ItemStack holding;
    private final boolean isUsage;

    public BlockInteractEvent(Block block) {
        this.block = block;
        this.causer = null;
        this.holding = null;
        this.isUsage = false;
    }

    public BlockInteractEvent(Block block, Entity causer, ItemStack holding, boolean isUsage) {
        this.block = block;
        this.causer = causer;
        this.holding = holding;
        this.isUsage = isUsage;
    }

    public Block getBlock() {
        return block;
    }

    public Entity getCauser() {
        return causer;
    }

    public ItemStack getHolding() {
        return holding;
    }

    public boolean isUsage() {
        return isUsage;
    }

    public boolean isPlayerCaused() {
        return causer instanceof Player
                && (!((Player) causer).getName().startsWith("["));
    }

    public boolean isWorldCaused() {
        return causer == null
                || (isPlayerCaused() || ((Player) causer).getName().startsWith(
                        "["));
    }

    @Override
    public void call(ProtectionListener listener) {
        listener.onEvent(this);
    }

}
