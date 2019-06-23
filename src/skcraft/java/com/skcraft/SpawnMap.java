/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft;

import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapCursor;
import org.bukkit.map.MapCursorCollection;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.declarative.DefaultInt;
import com.sk89q.rebar.config.declarative.Setting;
import com.sk89q.rebar.config.declarative.SettingBase;

public class SpawnMap extends AbstractComponent {

    private static final short MAP_INDEX = 2;
    private static Image img;
    private final SpawnMapRenderer renderer = new SpawnMapRenderer();
    private LocalConfiguration config;

    static {
        try {
            InputStream stream = new FileInputStream(new File(Rebar
                    .getInstance().getDataFolder(), "spawn_map.png"));
            if (stream != null) {
                img = ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        config = configure(new LocalConfiguration());
        Rebar.getInstance().registerEvents(this);

        MapView map = Rebar.getInstance().getServer().getMap(MAP_INDEX);
        if (map != null) {
            setupMap(map);
        }
    }

    @Override
    public void shutdown() {
    }

    private void setupMap(MapView map) {
        for (MapRenderer renderer : map.getRenderers()) {
            map.removeRenderer(renderer);
        }
        map.addRenderer(renderer);
    }

    public static boolean isHoldingMap(Player player) {
        return player.getItemInHand().getType() == Material.MAP
                && player.getItemInHand().getDurability() == MAP_INDEX;
    }

    @EventHandler
    public void onMapInitialize(MapInitializeEvent event) {
        if (event.getMap().getId() == MAP_INDEX) {
            setupMap(event.getMap());
        }
    }

    public class SpawnMapRenderer extends MapRenderer {
        private int renderIndex = 0;

        public SpawnMapRenderer() {
            super(true);
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            if (player.getItemInHand().getType() != Material.MAP) {
                return;
            }

            if (player.getItemInHand().getDurability() != MAP_INDEX) {
                return;
            }

            Location loc = player.getLocation();
            int x = (loc.getBlockX() - config.centerX) / config.scale;
            int z = (loc.getBlockZ() - config.centerZ) / config.scale;
            boolean visible = (x >= -128 || x <= 127) && (z >= -128 || z <= 127);

            byte direction = (byte) (((((loc.getYaw() + 360) % 360) / 360 * 15) + 1) % 15);

            MapCursorCollection cursors = canvas.getCursors();
            if (cursors.size() == 0) {
                if (visible) {
                    cursors.addCursor(x, z, direction, (byte)2, visible);
                }
            } else {
                MapCursor cursor = cursors.getCursor(0);
                if (visible) {
                    cursor.setX((byte) x);
                    cursor.setY((byte) z);
                    cursor.setDirection(direction);
                }
                cursor.setVisible(visible);
            }

            renderIndex++;
            if (renderIndex % 10 != 0) return;
            renderIndex = 0;

            if (canvas.getPixel(0, 0) == -1 && img != null) {
                canvas.drawImage(0, 0, img);
            }
        }

    }

    @SettingBase("spawn-map")
    public class LocalConfiguration extends ConfigurationBase {
        @Setting("centerX") @DefaultInt(0)
        public Integer centerX;

        @Setting("centerZ") @DefaultInt(0)
        public Integer centerZ;

        @Setting("scale") @DefaultInt(1)
        public Integer scale;
    }

}
