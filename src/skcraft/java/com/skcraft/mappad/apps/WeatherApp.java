/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.apps;

import java.util.Random;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import com.skcraft.mappad.AbstractApplication;
import com.skcraft.mappad.ImageResource;
import com.skcraft.mappad.MapPad;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.MapCanvasUtil;
import com.sk89q.rebar.util.TimeUtil;

public class WeatherApp extends AbstractApplication {

    private static Random random = new Random();
    private static ImageResource sunnyIcon = new ImageResource("/resources/mappad_weather_sunny.png");
    private static ImageResource rainIcon = new ImageResource("/resources/mappad_weather_rain.png");
    private static ImageResource thunderIcon = new ImageResource("/resources/mappad_weather_thunder.png");
    
    private long lastDraw = 0;
    private boolean hasDrawnInitial = false;
    private WeatherType lastWeather = null;
    private WeatherType weatherForecast = WeatherType.values()[random.nextInt(WeatherType.values().length)];
    
    enum WeatherType {
        SUNNY, RAINING, THUNDERING;
    }

    public WeatherApp(MapPad mapPad, Player player) {
        super(mapPad, player);
    }

    public void draw(MapCanvas canvas) {
        if (!hasDrawnInitial) {
            MapCanvasUtil.clear(canvas, (byte) 34);
            drawTitle(canvas, "Weather");
            drawWeather(canvas, 60, weatherForecast, "Tomorrow:");
            MapCanvasUtil.drawText(canvas, 5, 100, "Forecasts 33% accurate", MapPalette.DARK_GRAY);
            hasDrawnInitial = true;
        }
        
        long now = System.currentTimeMillis();
        World world = getPlayer().getWorld();
        WeatherType weather = getWeather(world);
        
        if (now - lastDraw < 5000 && weather == lastWeather) {
            return;
        }
        
        MapCanvasUtil.clear(canvas, 5, 15, 40, 10, (byte) 34);
        MapCanvasUtil.drawText(canvas, 5, 15, TimeUtil.getTimeString(world.getTime()), MapPalette.DARK_GRAY);
        
        if (weather != lastWeather) {
            weatherForecast = WeatherType.values()[random.nextInt(WeatherType.values().length)];
            drawWeather(canvas, 60, weatherForecast, "Tomorrow:");
            drawWeather(canvas, 30, weather, "Currently:");
            lastWeather = weather;
        }
        
        lastDraw = now;
    }

    public void accept(CommandContext context) throws CommandException {
        if (context.matches("help")) {
            print("Weather and time app. Refreshes automatically.");
        } else {
            throw new CommandException("Unknown command! Try >help");
        }
    }

    public void quit() {
    }
    
    private WeatherType getWeather(World world) {
        if (!world.hasStorm()) {
            return WeatherType.SUNNY;
        } else if (!world.isThundering()) {
            return WeatherType.RAINING;
        } else {
            return WeatherType.THUNDERING;
        }
    }
    
    private void drawWeather(MapCanvas canvas, int y, WeatherType type, String text) {
        ImageResource img;
        String message;
        
        if (type == WeatherType.SUNNY) {
            img = sunnyIcon;
            message = "Sunny";
        } else if (type == WeatherType.RAINING) {
            img = rainIcon;
            message = "Raining";
        } else {
            img = thunderIcon;
            message = "Thundering";
        }
        
        img.draw(canvas, 5, y);
        MapCanvasUtil.clear(canvas, 35, y + 5, 128 - 35, 20, (byte) 34);
        MapCanvasUtil.drawText(canvas, 35, y + 5, text, MapPalette.DARK_GRAY);
        MapCanvasUtil.drawText(canvas, 35, y + 15, message, MapPalette.DARK_GRAY);
    }

}
