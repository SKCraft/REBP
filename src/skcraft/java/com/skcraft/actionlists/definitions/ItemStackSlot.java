/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import org.bukkit.inventory.ItemStack;

/**
 * Indicates an item stack slot that can be updated with a new stack.
 *
 * @author sk89q
 */
public interface ItemStackSlot {

    /**
     * Get the current item stack.
     *
     * @return item stack, or null
     */
    ItemStack get();

    /**
     * Update the slot with a new item stack.
     *
     * @param stack the new item stack
     */
    void update(ItemStack stack);

}
