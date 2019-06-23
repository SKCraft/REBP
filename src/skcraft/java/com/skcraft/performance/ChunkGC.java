/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.helpers.InjectComponent;

public class ChunkGC extends AbstractComponent {

    private Logger logger = createLogger(ChunkGC.class);

    @InjectComponent
    private ClockMonitor monitor;
    private long lastGCTime = 0;

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(GCCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public boolean gcChunks() {
        return gcChunks(false);
    }

    public boolean gcChunks(boolean force) {
        Server server = Rebar.server();
        int chunksUnloaded = 0;
        int chunksTotal = 0;
        double rate = monitor.getSnapshot().getTicksPerSecond()[2];
        long now = System.currentTimeMillis();

        if (!force && rate >= 19) {
            return false;
        }

        if (!force && now - lastGCTime <= 1000 * 60 * 30) {
            return false;
        }

        lastGCTime = now;

        logger.info(String.format("ChunkGC: Current tick rate is %g ticks/second", rate));

        for (World world : server.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                chunksTotal++;
                if (chunk.unload(true, true)) {
                    chunksUnloaded++;
                }
            }
        }

        logger.info(String.format("ChunkGC: %d chunks out of %d unloaded", chunksUnloaded, chunksTotal));

        return true;
    }

    public static class GCCommands {
        private ChunkGC component;

        public GCCommands(ChunkGC component) {
            this.component = component;
        }

        @Command(aliases = {"chunkgc"}, min = 0, max = 0, desc = "Unload chunks", flags = "f")
        @CommandPermissions("skcraft.perf.unload-chunks")
        public void unloadChunks(CommandContext context, CommandSender sender)
                throws CommandException {
            if (!component.gcChunks(context.hasFlag('f'))) {
                throw new CommandException("No need to unload chunks at this time.");
            }
        }
    }

}
