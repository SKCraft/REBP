/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractAmplifier implements Amplifier {

    protected static final int TOP_LEFT = 0;
    protected static final int TOP_RIGHT = 2;
    protected static final int MIDDLE = 4;
    protected static final int BOTTOM_LEFT = 6;
    protected static final int BOTTOM_RIGHT = 8;
    
    public boolean isItem(Inventory inven, int slot, Material mat) {
        ItemStack item = inven.getItem(slot);
        if (item == null) return mat == null || mat == Material.AIR;
        if (item.getType() == Material.AIR) return mat == null || mat == Material.AIR;
        if (item.getType() != mat) return mat == null;
        return true;
    }
    
    public boolean isRecipe(Inventory inven, Material topLeft,
            Material topRight, Material middle, Material bottomLeft, Material bottomRight) {
        return isItem(inven, TOP_LEFT, topLeft) &&
                isItem(inven, TOP_RIGHT, topRight) &&
                isItem(inven, MIDDLE, middle) &&
                isItem(inven, BOTTOM_LEFT, bottomLeft) &&
                isItem(inven, BOTTOM_RIGHT, bottomRight);
    }
    
}
