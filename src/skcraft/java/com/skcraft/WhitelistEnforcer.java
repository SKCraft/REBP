/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.RequiresComponent;
import com.sk89q.rebar.components.GuestProtectedEvent;
import com.sk89q.rebar.components.GuestProtection;
import com.sk89q.rebar.event.RegisteredEvent;

@RequiresComponent(GuestProtection.class)
public class WhitelistEnforcer extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new ProtectionListener());
        Rebar.getInstance().registerEvents(new PlayerListener());
    }

    @Override
    public void shutdown() {
    }

    public static boolean isAuthorized(Player player) {
        return Rebar.getInstance().hasPermission(player, "build");
    }

    public class PlayerListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            /*if (!isAuthorized(event.getPlayer())) {
                ChatUtil.msg(event.getPlayer(), ChatColor.DARK_RED,
                        "**WELCOME** You are presently NOT on the whitelist. Please " +
                        "ask your friend on here to type /invite <your name> OR if this is in error, " +
                        "please contact sk89q. To apply to the server, visit http://skq.me/applymc");
            }*/
        }
    }

    public class ProtectionListener extends com.sk89q.rebar.components.ProtectionListener {
        @Override
        @RegisteredEvent(type = GuestProtectedEvent.class)
        public void onEvent(GuestProtectedEvent event) {
            if (!isAuthorized(event.getPlayer())) {
                event.cancel();
            }
        }
    }

}
