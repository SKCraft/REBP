/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.messages;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.components.sessions.Sessions;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.cardinal.Cardinal;
import com.skcraft.cardinal.service.notice.Notice;
import com.skcraft.cardinal.service.notice.NoticeManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DelayedMotd extends AbstractComponent implements Runnable, Listener {

    private static final Logger log = Logger.getLogger(ActionBarMessages.class.getName());
    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private NoticeManager noticeManager;
    private LocalConfiguration config;
    private String motd = null;

    @InjectComponent
    private Sessions sessions;

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(this);
        config = configure(new LocalConfiguration());
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        noticeManager = cardinal.getInstance(NoticeManager.class);
        Rebar.getInstance().registerEvents(this);
        timer.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        Futures.addCallback(noticeManager.getNext("motd"), new FutureCallback<Notice>() {
            @Override
            public void onSuccess(@Nullable Notice result) {
                if (result != null) {
                    motd = ChatUtil.replaceColorMacros(result.getMessage().replace("\r", ""));
                } else {
                    motd = null;
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public static class MotdSession {
        boolean hasSeen = false;
        int blocksMoved = 0;
    }

    @SettingBase("motd")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("threshold") @DefaultInt(20)
        public Integer threshold;

        @Override
        public void populate(Configuration config) {
            motd = motd != null ? ChatUtil.replaceColorMacros(motd) : null;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        String motd = this.motd;
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (motd != null && event.getFrom().getBlock().equals(to.getBlock())) {
            MotdSession session = sessions.get(player, MotdSession.class);
            if (!session.hasSeen) {
                session.blocksMoved++;
                if (session.blocksMoved > config.threshold) {
                    player.sendMessage(motd);
                    session.hasSeen = true;
                }
            }
        }
    }

}
