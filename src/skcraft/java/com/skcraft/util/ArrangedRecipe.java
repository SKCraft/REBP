/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.inventory.ItemStack;

public class ArrangedRecipe implements CraftingRecipe {

    private ItemStack[] ingredients;
    private ItemStack result;
    private int width;
    private int height;

    public ArrangedRecipe(ItemStack[] ingredients, ItemStack result, int width, int height) {
        super();
        this.ingredients = ingredients;
        this.result = result;
        this.width = width;
        this.height = height;
    }

    @Override
    public String getIngredientsText() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < ingredients.length; i++) {
            if (i != 0 && i % width == 0) {
                builder.append("\n");
            }
            ItemStack ingredient = ingredients[i];
            builder.append(getName(ingredient) + " ");
            builder.append(" ");
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private String getName(ItemStack item) {
        if (item == null) {
            return "---";
        }
        if (item.getDurability() < 1) {
            return item.getType().name();
        } else {
            return item.getType().name() + ":" + item.getDurability();
        }
    }

}
