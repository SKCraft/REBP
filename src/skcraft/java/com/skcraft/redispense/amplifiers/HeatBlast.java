/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense.amplifiers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import com.skcraft.redispense.AbstractAmplifier;

public class HeatBlast extends AbstractAmplifier {

    public boolean matches(Inventory inven) {
        return isRecipe(inven, 
                Material.AIR, Material.AIR,
                Material.NETHERRACK,
                Material.AIR, Material.AIR);
    }

    public boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                for (int y = -3; y <= 3; y++) {
                    Block cur = block.getRelative(x, y, z);
                    if (cur.getType() == Material.SNOW) {
                        cur.setType(Material.AIR);
                    } else if (cur.getType() == Material.ICE) {
                        cur.setType(Material.STATIONARY_WATER);
                    }
                }
            }
        }
        
        return true;
    }

}
