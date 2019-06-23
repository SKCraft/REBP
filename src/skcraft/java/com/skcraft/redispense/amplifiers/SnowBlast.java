/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense.amplifiers;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import com.skcraft.redispense.AbstractAmplifier;

public class SnowBlast extends AbstractAmplifier {
    
    private static final Random rand = new Random();

    public boolean matches(Inventory inven) {
        return isRecipe(inven, 
                Material.AIR, Material.AIR,
                Material.SNOW_BALL,
                Material.AIR, Material.AIR);
    }

    public boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel) {
        World world = block.getWorld();
        Location loc;
        if (block.getRelative(0, 1, 0).getType() == Material.AIR) {
            loc = block.getLocation().add(0.5, 1.5, 0.5);
        } else {
            loc = block.getLocation().add(0.5, 2.5, 0.5);
        }
        Snowball snowball = world.spawn(loc, Snowball.class);
        snowball.setVelocity(new Vector(rand.nextDouble() * 0.2 - 0.1, rand.nextDouble() * 0.2 + 0.3, rand.nextDouble() * 0.2 - 0.1));
        
        /*for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Block prev = block.getRelative(x, -4, z);
                for (int y = -3; y <= 3; y++) {
                    Block cur = block.getRelative(x, y, z);
                    if (cur.getType() == Material.AIR &&
                            !BlockType.canPassThrough(prev.getTypeId()) &&
                            prev.getType() != Material.WATER &&
                            prev.getType() != Material.STATIONARY_WATER &&
                            prev.getType() != Material.LAVA &&
                            prev.getType() != Material.STATIONARY_LAVA &&
                            prev.getType() != Material.ICE) {
                        cur.setType(Material.SNOW);
                    } else if (cur.getType() == Material.WATER || cur.getType() == Material.STATIONARY_WATER) {
                        cur.setType(Material.ICE);
                    }
                    prev = cur;
                }
            }
        }*/
        
        return true;
    }

}
