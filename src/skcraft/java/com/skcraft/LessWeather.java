/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Random;

public class LessWeather extends AbstractComponent {

    private static final Random rand = new Random();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new BlockListener());
    }

    @Override
    public void shutdown() {
    }

    public class BlockListener implements Listener {
        @EventHandler
        public void onWeatherChange(WeatherChangeEvent event) {
            if (event.isCancelled()) return;
            if (event.toWeatherState()) {
                if (rand.nextInt(2) != 0) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

}
