/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense.amplifiers;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.sk89q.rebar.util.BlockUtil;
import com.skcraft.redispense.AbstractAmplifier;

public class XPBottler extends AbstractAmplifier {

    public boolean matches(Inventory inven) {
        return isRecipe(inven, 
                Material.AIR, Material.AIR,
                Material.GLASS_BOTTLE,
                Material.AIR, Material.AIR);
    }

    public boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel) {
        World world = block.getWorld();

        Chunk chunk = block.getChunk();
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        
        List<ExperienceOrb> foundOrbs = new ArrayList<ExperienceOrb>();
        int amountFound = 0;

        found:
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (Entity ent : world.getChunkAt(chunkX + x, chunkZ + z).getEntities()) {
                    if (ent instanceof ExperienceOrb &&
                            ent.getLocation().distanceSquared(block.getLocation()) <= 25) {
                        ExperienceOrb orb = (ExperienceOrb) ent;
                        foundOrbs.add(orb);
                        amountFound += orb.getExperience();
                        
                        if (amountFound >= 6) {
                            break found;
                        }
                    }
                }
            }
        }

        if (amountFound < 6) {
            return true;
        }
            
        for (ExperienceOrb orb : foundOrbs) {
            orb.remove();
        }
        
        ItemStack item = new ItemStack(Material.EXP_BOTTLE, 1);
        org.bukkit.material.Dispenser matData = BlockUtil.getMaterialData(
                block, org.bukkit.material.Dispenser.class);
        BlockFace face = matData.getFacing();
        Vector v = new Vector(face.getModX(), face.getModY(), face.getModZ());
        world.dropItem(dispenser.getLocation().add(v).add(0.5, 0.5, 0.5), item);

        return true;
    }

}
