/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class ChunkLoadVisualizer extends AbstractComponent {

    private final ChunkRenderer renderer = new ChunkRenderer();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new ServerListener());

        MapView map = Rebar.getInstance().getServer().getMap((short)1);
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

    private class ChunkRenderer extends MapRenderer {

        private int renderIndex = 0;

        public ChunkRenderer() {
            super(true);
        }

        @Override
        public void render(MapView map, MapCanvas canvas, Player player) {
            renderIndex++;
            if (renderIndex % 10 != 0) return;
            renderIndex = 0;

            if (player.getItemInHand().getType() != Material.MAP) {
                return;
            }

            if (player.getItemInHand().getDurability() != 1) {
                return;
            }

            if (!Rebar.getInstance().hasPermission(player, "skcraft.chunk-visualize")) {
                return;
            }

            World world = player.getWorld();
            Chunk base = player.getLocation().getBlock().getChunk();

            for (int x = 0; x < 128; x++) {
                for (int y = 0; y < 128; y++) {
                    int chunkX = base.getX() + x - 64;
                    int chunkZ = base.getZ() + y - 64;

                    boolean loaded = world.isChunkLoaded(chunkX, chunkZ);
                    byte lastColor = canvas.getPixel(x, y);

                    byte color = MapPalette.TRANSPARENT;
                    if (loaded) {
                        Chunk c = world.getChunkAt(chunkX, chunkZ);
                        color = MinecartChunkLoader.hasKeepAliveEntities(c) ? MapPalette.LIGHT_GREEN : MapPalette.RED;
                    }

                    if (lastColor != color) {
                        canvas.setPixel(x, y, color);
                    }
                }
            }
        }

    }

    public class ServerListener implements Listener {
        @EventHandler
        public void onMapInitialize(MapInitializeEvent event) {
            if (event.getMap().getId() == 1) {
                setupMap(event.getMap());
            }
        }
    }

}
