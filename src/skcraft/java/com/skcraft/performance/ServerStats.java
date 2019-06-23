/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.management.ManagementUtils;
import com.sk89q.rebar.management.ServerStatsMXBean;
import com.sk89q.worldguard.bukkit.BukkitUtil;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.HashMap;
import java.util.Map;

public class ServerStats extends AbstractComponent implements ServerStatsMXBean {

    private Map<String, Integer> cachedEntityCounts;
    private long entityCountTime = 0;

    @Override
    public void initialize() {
        ManagementUtils.register(this, "com.sk89q.skcraft.performance:type=ServerStats");
    }

    @Override
    public void shutdown() {
    }

    @Override
    public int getWorldCount() {
        return Rebar.getInstance().getServer().getWorlds().size();
    }

    @Override
    public int getLoadedChunkCount() {
        int numChunks = 0;

        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            numChunks += world.getLoadedChunks().length;
        }

        return numChunks;
    }

    @Override
    public Map<String, Integer> getWeatherDurations() {
        Map<String, Integer> durations = new HashMap<String, Integer>();

        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            durations.put(world.getName(), world.getWeatherDuration());
        }

        return durations;
    }

    @Override
    public Map<String, Integer> getThunderDurations() {
        Map<String, Integer> durations = new HashMap<String, Integer>();

        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            durations.put(world.getName(), world.getThunderDuration());
        }

        return durations;
    }

    @Override
    public synchronized Map<String, Integer> getEntityCounts() {
        long now = System.currentTimeMillis();
        if (now - entityCountTime < 1000) {
            return cachedEntityCounts;
        }
        entityCountTime = now;

        int numEntities = 0;
        int numLiving = 0;
        int numNonLiving = 0;
        int numPassive = 0;
        int numMonsters = 0;
        int numAmbient = 0;
        int numHumans = 0;
        int numPlayers = 0;
        int numItems = 0;
        int numSheep = 0;
        int numPigs = 0;
        int numCows = 0;
        int numChickens = 0;
        int numWolves = 0;
        int numXPOrbs = 0;
        int numFalling = 0;
        int numHanging = 0;
        int numProjectile = 0;

        for (World world : Rebar.getInstance().getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities()) {
                    numEntities++;

                    if (entity instanceof Player) {
                        numPlayers++;
                    } else if (entity instanceof LivingEntity) {
                        numLiving++;

                        if (entity instanceof HumanEntity) {
                            numHumans++;
                        } else if (entity instanceof Animals) {
                            numPassive++;

                            if (entity instanceof Sheep) {
                                numSheep++;
                            } else if (entity instanceof Sheep) {
                                numPigs++;
                            } else if (entity instanceof Cow) {
                                numCows++;
                            } else if (entity instanceof Chicken) {
                                numChickens++;
                            } else if (entity instanceof Wolf) {
                                numWolves++;
                            }
                        } else if (entity instanceof Monster || entity instanceof Slime
                                || entity instanceof MagmaCube || entity instanceof Flying) {
                            numMonsters++;
                        } else if (entity instanceof Ambient) {
                            numAmbient++;
                        }
                    } else {
                        numNonLiving++;

                        if (entity instanceof ExperienceOrb) {
                            numXPOrbs++;
                        } else if (entity instanceof FallingBlock) {
                            numFalling++;
                        } else if (entity instanceof Hanging) {
                            numHanging++;
                        } else if (entity instanceof Projectile) {
                            numProjectile++;
                        }
                    }

                    if (entity instanceof Item) {
                        numItems++;
                    }
                }
            }
        }

        Map<String, Integer> data = new HashMap<String, Integer>();
        data.put("entities", numEntities);
        data.put("living", numLiving);
        data.put("nonLiving", numNonLiving);
        data.put("passive", numPassive);
        data.put("monsters", numMonsters);
        data.put("ambient", numAmbient);
        data.put("humans", numHumans);
        data.put("players", numPlayers);
        data.put("items", numItems);
        data.put("sheep", numSheep);
        data.put("pigs", numPigs);
        data.put("cows", numCows);
        data.put("chickens", numChickens);
        data.put("wolves", numWolves);
        data.put("xp", numXPOrbs);
        data.put("falling", numFalling);
        data.put("hanging", numHanging);
        data.put("projectiles", numProjectile);

        return cachedEntityCounts = data;
    }

    @Override
    public int getPlayerCount() {
        return BukkitUtil.getOnlinePlayers().size();
    }

    @Override
    public int getEntityCount() {
        return getEntityCounts().get("entities");
    }

    @Override
    public int getLivingEntityCount() {
        return getEntityCounts().get("living");
    }

    @Override
    public int getItemEntityCount() {
        return getEntityCounts().get("items");
    }

}
