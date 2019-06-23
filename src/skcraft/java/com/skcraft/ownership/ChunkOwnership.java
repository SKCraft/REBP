/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.LazyPluginReference;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.helpers.InjectComponent;
import com.sk89q.rebar.helpers.InjectPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.skcraft.InteractivePrompt;
import com.skcraft.cardinal.Cardinal;
import com.skcraft.cardinal.service.claim.ClaimCache;
import com.skcraft.cardinal.service.party.PartyCache;
import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.util.MagicWand;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChunkOwnership extends AbstractComponent implements Listener {

    private ChunkOwnershipConfig config;
    private ExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @InjectPlugin(WorldEditPlugin.class) private LazyPluginReference<WorldEditPlugin> worldEdit;
    @InjectComponent private InteractivePrompt prompter;
    @InjectComponent private MagicWand magicWand;
    private ClaimCache claimCache;
    private PartyCache partyCache;

    @Override
    public void initialize() {
        config = configure(new ChunkOwnershipConfig());

        Cardinal cardinal = Cardinal.load();
        claimCache = cardinal.getInstance(ClaimCache.class);
        partyCache = cardinal.getInstance(PartyCache.class);

        Rebar.getInstance().registerCommands(OwnershipCommands.class, this);
        Rebar.getInstance().registerEvents(this);
        Rebar.getInstance().registerEvents(new OwnershipListener(claimCache));

        magicWand.register(new OwnershipWandFactory(this), "owner", "ownership");

        preloadWorlds();
    }

    @Override
    public void shutdown() {
    }

    public ClaimCache getClaimCache() {
        return claimCache;
    }

    public PartyCache getPartyCache() {
        return partyCache;
    }

    public void preloadWorlds() {
        for (World world : Rebar.server().getWorlds()) {
            preloadChunks(world);
        }
    }

    public void preloadChunks(World world) {
        for (Chunk chunk : world.getLoadedChunks()) {
            claimCache.queueChunk(new WorldVector3i(world.getName(), chunk.getX(), 0, chunk.getZ()));
        }
    }

    Selection getSelection(Player player) {
        return worldEdit.get().getSelection(player);
    }

    InteractivePrompt getPrompter() {
        return prompter;
    }

    ChunkOwnershipConfig getConfiguration() {
        return config;
    }

    ExecutorService getExecutor() {
        return executor;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        preloadChunks(event.getWorld());
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        claimCache.invalidateChunksInWorld(event.getWorld().getName());
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        claimCache.queueChunk(new WorldVector3i(event.getWorld().getName(), event.getChunk().getX(), 0, event.getChunk().getZ()));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        claimCache.invalidateChunk(new WorldVector3i(event.getWorld().getName(), event.getChunk().getX(), 0, event.getChunk().getZ()));
    }

}
