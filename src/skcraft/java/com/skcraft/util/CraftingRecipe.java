/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.inventory.ItemStack;

public interface CraftingRecipe {
    
    public String getIngredientsText();
    
    public ItemStack getResult();
    
    public ItemStack[] getIngredients();

}
