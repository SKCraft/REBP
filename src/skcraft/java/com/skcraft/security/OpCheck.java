/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.security;

import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.Unlisted;

@Unlisted
public class OpCheck extends AbstractComponent {

    private Logger logger = createLogger(OpCheck.class);

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    private class Listener implements org.bukkit.event.Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerLogin(PlayerLoginEvent event) {
            Player player = event.getPlayer();
            if (player.isOp()) {
                logger.warning(player.getName() + " joined with op -- op removed!");
                event.getPlayer().setOp(false);
            }
        }

    }

}
