/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.Context;
import com.skcraft.actionlists.SubjectResolver;

public class ItemHeldSubjectResolver implements SubjectResolver<ItemStackSlot> {

    @Override
    public ItemStackSlot resolve(Context context) {
        Entity entity = ((BukkitContext) context).getSource();
        if (entity != null && entity instanceof HumanEntity) {
            final PlayerInventory inventory = ((HumanEntity) entity).getInventory();
            ItemStack item = inventory.getItemInHand();

            if (item == null) {
                item = new ItemStack(0);
            }

            final ItemStack finalItem = item;

            return new ItemStackSlot() {
                @Override
                public void update(ItemStack stack) {
                    inventory.setItemInHand(stack);
                }

                @Override
                public ItemStack get() {
                    return finalItem;
                }
            };
        }

        return null;
    }

}
