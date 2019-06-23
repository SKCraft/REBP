/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultString;
import com.sk89q.rebar.config.declarative.Setting;

public class CustomMotdLine extends AbstractComponent {

    private LocalConfiguration config;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new ServerListener());
        config = configure(new LocalConfiguration());
    }

    @Override
    public void shutdown() {
    }

    public class ServerListener implements Listener {
        @EventHandler
        public void onServerListPing(ServerListPingEvent event) {
            event.setMotd(config.motd);
        }
    }

    public class LocalConfiguration extends ConfigurationBase {
        @Setting("server-list-line") @DefaultString("http://reddit.com/r/skminecraft")
        public String motd;

        @Override
        public void populate(Configuration config) {
        }
    }
}
