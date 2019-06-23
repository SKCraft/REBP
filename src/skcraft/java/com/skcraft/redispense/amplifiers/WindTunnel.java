/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense.amplifiers;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

import com.skcraft.redispense.AbstractAmplifier;

public class WindTunnel extends AbstractAmplifier {

    @Override
    public boolean matches(Inventory inven) {
        return isRecipe(inven,
                Material.AIR, Material.AIR,
                Material.DISPENSER,
                Material.AIR, Material.AIR);
    }

    public boolean isAffectedEntity(Entity ent) {
        return ent instanceof ExperienceOrb ||
                ent instanceof Projectile ||
                ent instanceof Explosive ||
                ent instanceof FallingBlock ||
                ent instanceof Item ||
                ent instanceof Projectile;
    }

    @Override
    public boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel) {
        World world = block.getWorld();
        Chunk chunk = block.getChunk();
        Location loc = block.getLocation();

        Vector addedVel = vel.multiply(Math.min(15, inven.getItem(MIDDLE).getAmount() - 1) * 0.1 + 1);
        int radius = 5;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (Entity ent : world.getChunkAt(chunk.getX() + x, chunk.getZ() + z).getEntities()) {
                    if (isAffectedEntity(ent) && loc.distanceSquared(ent.getLocation()) < radius * radius) {
                        ent.setVelocity(ent.getVelocity().add(addedVel));
                    }
                }
            }
        }

        return true;
    }

}
