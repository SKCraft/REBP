/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense.amplifiers;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.rebar.util.BlockUtil;
import com.skcraft.redispense.AbstractAmplifier;

public class Cutter extends AbstractAmplifier {

    public boolean matches(Inventory inven) {
        return isRecipe(inven, 
                Material.IRON_INGOT, Material.IRON_INGOT,
                Material.SHEARS,
                Material.IRON_INGOT, Material.IRON_INGOT);
    }

    public boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel) {
        World world = block.getWorld();

        org.bukkit.material.Dispenser matData = BlockUtil.getMaterialData(
                block, org.bukkit.material.Dispenser.class);
        BlockFace face = matData.getFacing();
        Block outputBlock = dispenser.getLocation().add(
                face.getModX(), face.getModY(), face.getModZ()).getBlock();
        
        Material mat = outputBlock.getType();
        ItemStack item = null;
        
        if (mat == Material.LEAVES) {
            int data = outputBlock.getData() & 3;
            item = new ItemStack(Material.LEAVES, 1, (short) data);
        } else if (mat == Material.DEAD_BUSH) {
            item = new ItemStack(Material.DEAD_BUSH, 1);
        } else if (mat == Material.LONG_GRASS) {
            int data = outputBlock.getData() & 3;
            item = new ItemStack(Material.LONG_GRASS, 1, (short) data);
        } else if (mat == Material.VINE) {
            item = new ItemStack(Material.VINE, 1);
        }
        
        if (item != null) {
            outputBlock.setType(Material.AIR);
            world.dropItem(outputBlock.getLocation().add(0.5, 0.5, 0.5), item);
            ItemStack shears = inven.getItem(4);
            short durability = shears.getDurability();
            if (durability >= 239) {
                inven.setItem(4, null);
            } else {
                shears.setDurability((short) (durability + 1));
                inven.setItem(4, shears);
            }
        }
        
        return true;
    }

}
