/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.Iterator;
import java.util.Set;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.ListType;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class NoExplosions extends AbstractComponent {

    private Configuration config;

    @Override
    public void initialize() {
        config = configure(new Configuration());
        Rebar.getInstance().registerEvents(new EntityListener());
    }

    @Override
    public void shutdown() {
    }

    public class EntityListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onEntityExplode(EntityExplodeEvent event) {
            if (config.blocks.size() == 0) {
                event.setCancelled(true);
            }

            Iterator<Block> it = event.blockList().iterator();
            while (it.hasNext()) {
                int typeId = it.next().getTypeId();
                if (!config.blocks.contains(typeId)) {
                    it.remove();
                }
            }
        }
    }

    @SettingBase("explosion-limiter")
    public class Configuration extends ConfigurationBase {
        @Setting("unsafe-blocks") @ListType(Integer.class)
        public Set<Integer> blocks;
    }

}
