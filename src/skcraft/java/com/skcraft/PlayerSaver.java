/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.entity.Player;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class PlayerSaver extends AbstractComponent {

    private LocalConfiguration config;

    @Override
    public void initialize() {
        config = configure(new LocalConfiguration());
        Rebar.getInstance().registerInterval(new Saver(), config.delay, config.delay);
    }

    @Override
    public void shutdown() {
    }

    private  class Saver implements Runnable {

        @Override
        public void run() {
            for (Player player : Rebar.getInstance().getServer().getOnlinePlayers()) {
                player.saveData();
            }
        }

    }

    @SettingBase("player-saver")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("delay") @DefaultInt(20 * 60 * 5)
        public Integer delay;

        @Override
        public void populate(Configuration config) {
        }
    }

}
