/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class Pokeballs extends AbstractComponent {

    private static final Random random = new Random();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    public boolean isCapturable(Entity entity) {
        return entity instanceof Animals && !(entity instanceof Tameable
                && ((Tameable) entity).isTamed()) && !(entity instanceof Ageable
                        && !((Ageable) entity).isAdult());
    }

    public class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
            if (event.isCancelled()) return;

            Entity inflicter = event.getDamager();
            Entity victim = event.getEntity();
            Location loc = victim.getLocation();
            World world = victim.getWorld();

            if (inflicter != null && inflicter instanceof Egg && isCapturable(victim)) {
                inflicter.setMetadata("pokeballs.captured",
                        new FixedMetadataValue(Rebar.getInstance(), true));
                inflicter.remove();
                event.setCancelled(true);

                int chance = (victim instanceof Tameable) ? 20 : 4;

                if (random.nextInt(chance) == 0) {
                    victim.remove();
                    ItemStack spawnEgg = new ItemStack(Material.MONSTER_EGG, 1);
                    spawnEgg.setDurability(new SpawnEgg(victim.getType()).getData());
                    world.dropItem(loc, spawnEgg);
                    world.playEffect(loc, Effect.POTION_BREAK, 0);
                } else {
                    world.playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0);
                }
            }
        }

        @EventHandler
        public void onPlayerEggThrow(PlayerEggThrowEvent event) {
            if (event.getEgg().hasMetadata("pokeballs.captured")) {
                event.setHatching(false);
            }
        }
    }

}
