/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import org.bukkit.inventory.ItemStack;

import com.skcraft.actionlists.Action;
import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.SubjectResolver;

public class UpdateItemAction implements Action<BukkitContext> {

    private SubjectResolver<ItemStackSlot> resolver;
    private boolean destroy = false;
    private short newData = -1;

    public UpdateItemAction(SubjectResolver<ItemStackSlot> resolver) {
        this.resolver = resolver;
    }

    public SubjectResolver<ItemStackSlot> getResolver() {
        return resolver;
    }

    public void setResolver(SubjectResolver<ItemStackSlot> resolver) {
        this.resolver = resolver;
    }

    public boolean isDestroy() {
        return destroy;
    }

    public void setDestroy(boolean destroy) {
        this.destroy = destroy;
    }

    public short getNewData() {
        return newData;
    }

    public void setNewData(short newData) {
        this.newData = newData;
    }

    @Override
    public void apply(BukkitContext context) {
        ItemStackSlot slot = resolver.resolve(context);
        ItemStack item = slot.get();
        boolean updated = false;

        if (item == null) {
            return;
        }

        if (destroy) {
            item = null;
            updated = true;
        } else if (newData >= 0) {
            item.setDurability(newData);
            updated = true;
        }

        if (updated) {
            slot.update(item);
        }
    }

}
