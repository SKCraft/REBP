/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.security;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.Unlisted;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultBoolean;
import com.sk89q.rebar.config.declarative.DefaultString;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

@Unlisted
public class ClientIdentityVerifier extends AbstractComponent {

    private Set<String> verified = new HashSet<String>();
    private VerifierConfiguration config;

    @Override
    public void initialize() {
        config = configure(new VerifierConfiguration());

        Rebar.getInstance().registerEvents(new Listener());
    }

    @Override
    public void shutdown() {
    }

    public boolean isVerified(Player player) {
        return true;
    }

    private class Listener implements org.bukkit.event.Listener {

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerLogin(PlayerLoginEvent event) {
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onPlayerQuit(PlayerQuitEvent event) {
            verified.remove(event.getPlayer().getName());
        }

    }

    @SettingBase("client-ident-verify")
    private class VerifierConfiguration extends ConfigurationBase {

        @Setting("deny") @DefaultBoolean(false)
        private Boolean deny;

        @Setting("deny-message") @DefaultString("Your identity is not verified.")
        private String denyMessage;

    }

}
