/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Sheep;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;

public class EntityReducer extends AbstractComponent implements Runnable {

    private static final int ANIMAL_THRESHOLD = 8;
    private static final int MONSTER_THRESHOLD = 8;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(ReducerCommands.class, this);
        Rebar.getInstance().registerInterval(this, 20*60*5, 20*60*5);
    }

    @Override
    public void shutdown() {
    }

    public int removeEntities(boolean includeAnimals) {
        Server server = Rebar.server();
        int entitiesRemoved = 0;

        for (World world : server.getWorlds()) {
            if (world.getEnvironment() != Environment.NORMAL) {
                continue;
            }

            for (Chunk chunk : world.getLoadedChunks()) {
                //chunksTotal++;

                int numSheep = 0;
                int numCows = 0;
                int numPigs = 0;
                int numChicken = 0;
                int numMonsters = 0;

                for (Entity entity : chunk.getEntities()) {
                    //totalEntities++;

                    if (includeAnimals) { // Reduce animal counts
                        if (entity instanceof Sheep) {
                            if (++numSheep > ANIMAL_THRESHOLD) {
                                entity.remove();
                                entitiesRemoved++;
                            }
                        } else if (entity instanceof Cow) {
                            if (++numCows > ANIMAL_THRESHOLD) {
                                entity.remove();
                                entitiesRemoved++;
                            }
                        } else if (entity instanceof Pig) {
                            if (++numPigs > ANIMAL_THRESHOLD) {
                                entity.remove();
                                entitiesRemoved++;
                            }
                        } else if (entity instanceof Chicken) {
                            if (++numChicken > ANIMAL_THRESHOLD) {
                                entity.remove();
                                entitiesRemoved++;
                            }
                        }
                    }

                    if (entity instanceof Monster) {
                        if (++numMonsters > MONSTER_THRESHOLD) {
                            entity.remove();
                            entitiesRemoved++;
                        }
                    }
                }
            }
        }

        /*logger.info(String.format("EntityReducer: %d/%d removed, %d chunks",
                entitiesRemoved, totalEntities, chunksTotal));*/

        return entitiesRemoved;
    }

    public static class ReducerCommands {
        private EntityReducer component;

        public ReducerCommands(EntityReducer component) {
            this.component = component;
        }

        @Command(aliases = {"reduceents"}, min = 0, max = 0, desc = "Reduce entities", flags = "a")
        @CommandPermissions("skcraft.perf.reduce-ents")
        public void unloadChunks(CommandContext context, CommandSender sender)
                throws CommandException {
            component.removeEntities(context.hasFlag('a'));
            ChatUtil.msg(sender, ChatColor.YELLOW + "Entities reduced.");
        }
    }

    @Override
    public void run() {
        removeEntities(true);
    }

}
