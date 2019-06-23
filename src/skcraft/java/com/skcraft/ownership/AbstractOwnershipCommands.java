/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.ownership;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.CommandRunnable;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.skcraft.cardinal.service.party.Party;
import com.skcraft.cardinal.util.WorldVector3i;
import lombok.Getter;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public abstract class AbstractOwnershipCommands {

    @Getter private final ChunkOwnership ownership;
    private Semaphore lock = new Semaphore(1);

    protected AbstractOwnershipCommands(ChunkOwnership ownership) {
        this.ownership = ownership;
    }

    protected List<Chunk> getSelection(World world, Player player) throws CommandException {
        return getSelection(world, player, true);
    }

    protected List<Chunk> getSelection(World world, Player player, boolean checkLimits) throws CommandException {
        ChunkOwnershipConfig config = getOwnership().getConfiguration();

        Selection selection = getOwnership().getSelection(player);
        Set<Vector2D> coords;

        // Check for selection
        if (selection == null) {
            throw new CommandException(
                    "You must make an area selection with WorldEdit first.");
        }

        // Check the length of the claim in one direction
        if (checkLimits && (selection.getWidth() > config.maxChunkLengthClaim * 16
                || selection.getLength() > config.maxChunkLengthClaim * 16)) {
            throw new CommandException(
                    "You can only claim up to " + config.maxChunkLengthClaim + " chunks in one direction at a time.");
        }

        // Get list of selected chunks
        try {
            coords = selection.getRegionSelector().getRegion().getChunks();
        } catch (IncompleteRegionException e) {
            throw new RuntimeException(e);
        }

        // Check the minimum list of chunks
        if (coords.size() == 0) {
            throw new CommandException("No chunks selected!");
        }

        // Check number of selected chunks
        if (checkLimits && coords.size() > config.maxChunkClaim) {
            throw new CommandException(
                    "You can only claim up to " + config.maxChunkClaim + " chunks at a time.");
        }

        // Change coordinates into chunks
        List<Chunk> chunks = new ArrayList<Chunk>();
        for (Vector2D coord : coords) {
            Chunk chunk = world.getChunkAt(coord.getBlockX(), coord.getBlockZ());

            double distanceSq = Math.pow(chunk.getX() * 16, 2)
                    + Math.pow(chunk.getZ() * 16, 2);

            // Is the chunk too far away from origin?
            if (checkLimits && distanceSq > config.maxDistance * config.maxDistance) {
                throw new CommandException(
                        "You cannot claim chunks this far away from origin (0, 0).");
            }

            // Is the chunk too close to origin?
            if (checkLimits && distanceSq < config.minDistance * config.minDistance) {
                throw new CommandException(
                        "You cannot claim chunks this close to origin (0, 0).");
            }

            chunks.add(chunk);
        }

        return chunks;
    }

    public List<WorldVector3i> toPositions(Collection<Chunk> chunks) {
        final List<WorldVector3i> positions = new ArrayList<>();
        for (Chunk chunk : chunks) {
            positions.add(new WorldVector3i(chunk.getWorld().getName(), chunk.getX(), 0, chunk.getZ()));
        }
        return positions;
    }

    protected Party getParty(String listName) throws CommandException {
        listName = listName.replaceAll("^#", "");

        Party party = getOwnership().getPartyCache().get(listName);
        if (party == null) {
            throw new CommandException(
                    "Could not find a party named '" + listName + "'.");
        }

        return party;
    }

    protected abstract class LockingCommandRunnable extends CommandRunnable {

        public LockingCommandRunnable(CommandContext context,
                CommandSender sender) throws CommandException {
            super(context, sender);
            if (!lock.tryAcquire()) {
                throw new CommandException("A request is already being served. Please try again shortly.");
            }
        }

        @Override
        public final void execute(CommandContext context, CommandSender sender)
                throws CommandException, InterruptedException, ExecutionException {
            try {
                executeBeforeRelease(context, sender);
            } finally {
                lock.release();
            }
        }

        public abstract void executeBeforeRelease(CommandContext context, CommandSender sender)
                throws CommandException, InterruptedException, ExecutionException;
    }

}
