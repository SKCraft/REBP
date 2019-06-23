/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.DefaultString;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.ChatUtil;

public class DelayedMotd extends AbstractComponent {

    private LocalConfiguration config;

    @InjectComponent
    private Sessions sessions;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new PlayerListener());
        config = configure(new LocalConfiguration());
    }

    @Override
    public void shutdown() {
    }

    public static class MotdSession {
        boolean hasSeen = false;
        int blocksMoved = 0;
    }

    @SettingBase("motd")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("motd") @DefaultString("This is the default MOTD.")
        public String motd;
        @Setting("threshold") @DefaultInt(20)
        public Integer threshold;

        @Override
        public void populate(Configuration config) {
            motd = motd != null ? ChatUtil.replaceColorMacros(motd) : null;
        }
    }

    public class PlayerListener implements Listener {
        @EventHandler
        public void onPlayerMove(PlayerMoveEvent event) {
            Player player = event.getPlayer();
            Location to = event.getTo();

            if (event.getFrom().getBlock().equals(to.getBlock())) {
                MotdSession session = sessions.get(player, MotdSession.class);
                if (!session.hasSeen) {
                    session.blocksMoved++;
                    if (session.blocksMoved > config.threshold) {
                        player.sendMessage(config.motd);
                        session.hasSeen = true;
                    }
                }
            }
        }
    }

}
