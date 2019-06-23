/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.World;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class WorldSaver extends AbstractComponent {

    private LocalConfiguration config;
    private Timer timer;

    @Override
    public void initialize() {
        config = configure(new LocalConfiguration());
        if (config.delay > 0) {
            timer = new Timer("World-Saver", true);
            timer.scheduleAtFixedRate(new Saver(), config.delay, config.delay);
        }
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void reload() {
        super.reload();
        if (timer != null) {
            timer.cancel();
        }
        if (config.delay > 0) {
            timer = new Timer("World-Saver", true);
            timer.scheduleAtFixedRate(new Saver(), config.delay, config.delay);
        }
    }

    private  class Saver extends TimerTask {
        @Override
        public void run() {
            Rebar.getInstance().registerTimeout(new RealSaver(), 0);
        }
    }

    private  class RealSaver implements Runnable {
        @Override
        public void run() {
            Rebar.server().broadcastMessage(ChatColor.DARK_GRAY + "(Please stand by. Forcing world save!)");
            for (World world : Rebar.server().getWorlds()) {
                world.save();
            }
        }
    }

    @SettingBase("world-saver")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("delay") @DefaultInt(1000 * 60 * 30)
        public Integer delay;

        @Override
        public void populate(Configuration config) {
        }
    }

}
