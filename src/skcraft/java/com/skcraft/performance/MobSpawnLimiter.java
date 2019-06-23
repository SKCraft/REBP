/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class MobSpawnLimiter extends AbstractComponent {

    private LimiterConfiguration config;

    @Override
    public void initialize() {
        config = configure(new LimiterConfiguration());

        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    @SettingBase("mob-spawn-limiter")
    private static class LimiterConfiguration extends ConfigurationBase {

        @Setting("mobs-of-entities-per-chunk") @DefaultInt(5)
        public Integer mobsOfEntitiesPerChunk;

    }

    private class Listener implements org.bukkit.event.Listener {

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onCreatureSpawn(CreatureSpawnEvent event) {
            if (event.getSpawnReason() == SpawnReason.NATURAL) {
                Entity entity = event.getEntity();

                if (entity instanceof Monster) {
                    int numEntities = event.getLocation().getChunk().getEntities().length;

                    if (numEntities > config.mobsOfEntitiesPerChunk) {
                        event.setCancelled(true);
                    }
                }
            }
        }

    }

}
