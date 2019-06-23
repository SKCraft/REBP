/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.inventory.ItemStack;

public class UnarrangedRecipe implements CraftingRecipe {

    private ItemStack[] ingredients;
    private ItemStack result;

    public UnarrangedRecipe(ItemStack[] ingredients, ItemStack result) {
        super();
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public String getIngredientsText() {
        StringBuilder builder = new StringBuilder("In any arrangement: ");

        boolean first = true;
        for (int i = 0; i < ingredients.length; i++) {
            if (!first) {
                builder.append(", ");
            }
            ItemStack ingredient = ingredients[i];
            builder.append(getName(ingredient));
            first = false;
        }

        return builder.toString();
    }

    @Override
    public ItemStack getResult() {
        return result;
    }

    @Override
    public ItemStack[] getIngredients() {
        return ingredients;
    }

    private String getName(ItemStack item) {
        if (item == null) {
            return "none";
        }
        if (item.getDurability() < 1) {
            return item.getType().name();
        } else {
            return item.getType().name() + ":" + item.getDurability();
        }
    }

}
