/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import com.sk89q.rebar.Rebar;

public class AdventureWorldManager {

    private static final Logger logger = Logger.getLogger(AdventureWorldManager.class.getCanonicalName());
    private static final Random random = new Random();
    private ActiveWorld active;
    private long expirationTime = -1;
    private List<WorldFactory> worldFactories = new ArrayList<WorldFactory>();
    private AdventureWorldManager self = this;

    public AdventureWorldManager() {
        Rebar.getInstance().registerEvents(new WorldListener());
    }

    public synchronized World getActive() {
        if (active == null) {
            logger.info("AdventureGates: Loading new active world...");
            createWorld();
            updateExpirationTime();
        }

        return active.getWorld();
    }

    private void createWorld() {
        WorldFactory factory = getRandomFactory();
        Rebar.getInstance()
                .getServer()
                .broadcastMessage(
                        ChatColor.GRAY + "(Please wait, loading world '"
                                + factory.getId()
                                + "'. The server is paused.)");
        String id = "world_@" + factory.getId();
        active = new ActiveWorld(id, null, factory);
        World world = factory.create(id);
        active.setWorld(world);
        world.setKeepSpawnInMemory(false);
        world.setSpawnFlags(true, false);
        world.setWaterAnimalSpawnLimit(0);
        Rebar.getInstance()
                .getServer()
                .broadcastMessage(
                        ChatColor.GRAY + "(World loading has completed!)");
    }

    private void updateExpirationTime() {
        expirationTime = System.currentTimeMillis()
                + 1000 * 60 * (random.nextInt(120) + 120);
    }

    @SuppressWarnings("unused")
    private boolean hasExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    private WorldFactory getRandomFactory() {
        // Better not be null
        return worldFactories.get(random.nextInt(worldFactories.size()));
    }

    public synchronized void register(WorldFactory factory) {
        worldFactories.add(factory);
    }

    public boolean isAdventureWorld(World world) {
        return world.getName().startsWith("world_@");
    }

    public boolean hasPeople(World world) {
        return world.getPlayers().size() > 0;
    }

    public class WorldListener implements Listener {
        @EventHandler
        public void onWorldInit(WorldInitEvent event) {
            synchronized (self) {
                if (active != null) {
                    if (event.getWorld().getName().equalsIgnoreCase(active.getName())) {
                        logger.info("AdventureGates: Initializing " + event.getWorld().getName());
                        active.getFactory().init(event.getWorld());
                    }
                }
            }
        }
    }

}
