/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.Criteria;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.util.MaterialPattern;

public class ItemCriteria implements Criteria<BukkitContext> {

    private SubjectResolver<ItemStackSlot> resolver;
    private MaterialPattern[] patterns = new MaterialPattern[0];
    private boolean hasData = false;

    public ItemCriteria(SubjectResolver<ItemStackSlot> resolver) {
        this.resolver = resolver;
    }

    public boolean hasDataCheck() {
        return hasData;
    }

    public void setDataCheck(boolean hasData) {
        this.hasData = hasData;
    }

    public SubjectResolver<ItemStackSlot> getResolver() {
        return resolver;
    }

    public void setResolver(SubjectResolver<ItemStackSlot> resolver) {
        this.resolver = resolver;
    }

    public MaterialPattern[] getPatterns() {
        return patterns;
    }

    public void setPatterns(MaterialPattern[] patterns) {
        this.patterns = patterns;
    }

    public void setPatterns(List<MaterialPattern> patterns) {
        MaterialPattern[] arr = new MaterialPattern[patterns.size()];
        patterns.toArray(arr);
        this.patterns = arr;
    }

    @Override
    public boolean matches(BukkitContext context) {
        ItemStack item = resolver.resolve(context).get();

        if (item == null) {
            return false;
        }

        if (hasData && item.getDurability() <= 0) {
            return false;
        }

        for (MaterialPattern pattern : patterns) {
            if (pattern.matches(item)) {
                return true;
            }
        }

        return false;
    }

}
