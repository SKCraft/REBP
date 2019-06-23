/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.event.AbstractEvent;

public class ItemUseEvent extends AbstractEvent<ProtectionListener> {

    private final ItemStack holding;
    private final Entity causer;

    public ItemUseEvent(ItemStack holding, Entity causer) {
        this.holding = holding;
        this.causer = causer;
    }

    public Entity getCauser() {
        return causer;
    }

    public ItemStack getHolding() {
        return holding;
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
