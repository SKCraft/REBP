/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class HardModeCompromiser extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    public class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled()) return;
            Entity ent = event.getEntity();
            if (!(ent instanceof Player)) return;
            Player player = (Player) ent;

            if (event.getCause() == DamageCause.STARVATION && player.getHealth() <= 1) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
