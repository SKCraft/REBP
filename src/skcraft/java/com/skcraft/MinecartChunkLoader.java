/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChunkUtil;
import com.sk89q.rebar.util.WorldChunkCoord;

public class MinecartChunkLoader extends AbstractComponent {

    private Map<WorldChunkCoord, Long> forceLoaded = new LinkedHashMap<WorldChunkCoord, Long>();

    @Override
    public void initialize() {
        Rebar.getInstance().registerEvents(new VehicleListener());
        Rebar.getInstance().registerEvents(new WorldListener());
        Rebar.getInstance().registerInterval(new ChunkUnloader(), 20, 20 * 4);
    }

    @Override
    public void shutdown() {
    }

    private static boolean isNearSpawn(Chunk chunk) {
        Location loc = chunk.getWorld().getSpawnLocation();
        double xDelta = loc.getX() - chunk.getX();
        double zDelta = loc.getZ() - chunk.getZ();
        return xDelta < 128 && xDelta > -128 && zDelta < 128 && zDelta > -128;
    }

    private void loadChunk(World world, int x, int z) {
        if (!world.isChunkLoaded(x, z)) {
            //Rebar.getInstance().getServer().broadcastMessage("LOADING CHUNK " + x + "," + z);
            if (world.loadChunk(x, z, false)) {
                forceLoaded.put(new WorldChunkCoord(world, x, z), System.currentTimeMillis());
            }
        }
    }

    public static boolean shouldKeepLoaded(Chunk chunk) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                Chunk c = ChunkUtil.getPotentialChunk(chunk.getWorld(), chunk.getX() + x, chunk.getZ() + z);
                if (c != null && hasKeepAliveEntities(c)) return true;
            }
        }

        return false;
    }

    public static boolean hasKeepAliveEntities(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof StorageMinecart && entity.getVelocity().lengthSquared() > 0.04) {
                return true;
            }
        }

        return false;
    }

    private class ChunkUnloader implements Runnable {
        @Override
        public void run() {
            long now = System.currentTimeMillis();

            Iterator<Map.Entry<WorldChunkCoord, Long>> it = forceLoaded.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<WorldChunkCoord, Long> entry = it.next();

                long deltaTime = now - entry.getValue();

                if (deltaTime > 1000 * 30) {
                    Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                    if (chunk != null && !isNearSpawn(chunk)) {
                        /*Rebar.getInstance().getServer().broadcastMessage("FORCING UNLOAD "
                                + entry.getKey().getX() + "," + entry.getKey().getZ());*/
                        chunk.unload(true, true);
                    }
                    it.remove();
                } else if (deltaTime > 1000 * 5) {
                    Chunk chunk = ChunkUtil.getPotentialChunk(entry.getKey());
                    if (chunk != null && !isNearSpawn(chunk) && !shouldKeepLoaded(chunk)) {
                        /*Rebar.getInstance().getServer().broadcastMessage("PROBABLY WANT TO UNLOAD "
                                + entry.getKey().getX() + "," + entry.getKey().getZ());*/
                        chunk.unload(true, true);
                    }
                    it.remove();
                }
            }
        }
    }

    public class WorldListener implements Listener {
        @EventHandler
        public void onChunkUnload(ChunkUnloadEvent event) {
            if (event.isCancelled()) return;

            if (forceLoaded.containsKey(new WorldChunkCoord(event.getChunk()))) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public class VehicleListener implements Listener {
        @EventHandler
        public void onVehicleMove(VehicleMoveEvent event) {
            Location from = event.getFrom();
            Location to = event.getTo();
            Vehicle vehicle = event.getVehicle();

            if (vehicle instanceof StorageMinecart) {
                if (from.getBlockX() != to.getBlockX()
                        || from.getBlockY() != to.getBlockY()
                        || from.getBlockZ() != to.getBlockZ()) {

                    Block block = to.getBlock();

                    /*Player[] players = Rebar.getInstance().getServer().getOnlinePlayers();
                    if (players.length > 0) {
                        int dist = (int) players[0].getLocation().distance(to);
                        Rebar.getInstance().getServer().broadcastMessage(to.getX() + "," + to.getY() + "," + to.getZ() + " (" + (dist / 16) + ") "
                                + block.getWorld().getLoadedChunks().length);
                    }*/

                    Chunk chunk = block.getChunk();
                    for (int x = -3; x <= 3; x++) {
                        for (int z = -3; z <= 3; z++) {
                            loadChunk(chunk.getWorld(), chunk.getX() + x, chunk.getZ() + z);
                        }
                    }
                }
            }
        }
    }

}
