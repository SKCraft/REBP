/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.StringUtil;

public class PerUserChunkLoader extends AbstractComponent implements Listener {

    public static final int MAX_LOADED_CHUNKS = 16;
    public static final String SIGN_LINE = "[LoadChunk]";
    public static final String SIGN_LINE_ALT = "[LoadChunks]";

    private Map<String, List<LoadedChunk>> loaded =
            new HashMap<String, List<LoadedChunk>>();

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(GCCommands.class, this);
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    public synchronized List<String> listNamesWithChunks() {
        List<String> names = new ArrayList<String>();
        for (String name : loaded.keySet()) {
            if (listChunks(name).size() > 0) {
                names.add(name);
            }
        }
        return names;
    }

    public synchronized List<LoadedChunk> listChunks(String name) {
        name = name.toLowerCase();
        List<LoadedChunk> chunks = loaded.get(name);

        if (chunks == null) {
            return new ArrayList<LoadedChunk>();
        }

        List<LoadedChunk> activeChunks = new ArrayList<LoadedChunk>();

        Iterator<LoadedChunk> it = chunks.iterator();
        while (it.hasNext()) {
            LoadedChunk chunk = it.next();
            if (!chunk.isLoaded()) {
                it.remove();
            } else {
                activeChunks.add(chunk);
            }
        }

        return activeChunks;
    }

    private synchronized boolean trackChunk(String name, Chunk chunk, Sign sign) {
        name = name.toLowerCase();
        if (name.length() > 15) {
            name = name.substring(0, 15);
        }

        List<LoadedChunk> chunks = loaded.get(name);

        if (chunks == null) {
            // Only load the chunks of players that are online
            if (Rebar.getInstance().getServer().getPlayerExact(name) == null) {
                return false;
            }

            chunks = new ArrayList<LoadedChunk>();
            chunks.add(new LoadedChunk(chunk, sign));
            loaded.put(name, chunks);
            return true;
        } else {
            if (chunks.contains(chunk)) {
                return true;
            }
        }

        Iterator<LoadedChunk> it = chunks.iterator();
        while (it.hasNext()) {
            LoadedChunk test = it.next();
            if (!test.isLoaded()) {
                it.remove();
            }
        }

        if (chunks.size() < MAX_LOADED_CHUNKS) {
            chunks.add(new LoadedChunk(chunk, sign));
            return true;
        }

        return false;
    }

    public boolean canUnload(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            if (state instanceof Sign) {
                Sign sign = (Sign) state;
                String line1 = ChatColor.stripColor(sign.getLine(0));
                String line2 = sign.getLine(1);

                if (line1 != null && line1.equalsIgnoreCase(SIGN_LINE)) {
                    if (trackChunk(String.valueOf(line2), chunk, sign)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public boolean tryUnloading(Chunk chunk) {
        if (canUnload(chunk)) {
            return chunk.unload(true, true);
        }

        return false;
    }

    public boolean unloadChunks(CommandSender sender) {
        Server server = Rebar.server();
        List<World> worlds = server.getWorlds();

        for (World world : server.getWorlds()) {
            if (world.getEnvironment() == Environment.NORMAL && !world.equals(worlds.get(0))) {
                continue; // Other worlds
            }

            int numChunks = 0;
            int numUnloaded = 0;

            for (Chunk chunk : world.getLoadedChunks()) {
                numChunks++;
                if (tryUnloading(chunk)) {
                    numUnloaded++;
                }
            }

            if (sender != null) {
                sender.sendMessage(ChatColor.GRAY
                        + String.format("For '%s', %d chunks out of %d unloaded",
                                world.getName(), numUnloaded, numChunks));
            }
        }

        return true;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSignChange(SignChangeEvent event) {
        if (event.getLine(0).equalsIgnoreCase(SIGN_LINE) ||
                event.getLine(0).equalsIgnoreCase(SIGN_LINE_ALT)) {
            event.setLine(0, ChatColor.AQUA + SIGN_LINE);
            event.setLine(1, event.getPlayer().getName());
            int priority = 0;
            try {
                priority = Integer.parseInt(event.getLine(3));
            } catch (NumberFormatException e) {
            }
            event.setLine(3, "Priority " + priority);
            event.getPlayer().sendMessage(ChatColor.GOLD +
                    "Now this chunk will not be unloaded, up to a limit of " +
                    MAX_LOADED_CHUNKS + " of these chunks with these signs with your name on it.");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!canUnload(event.getChunk())) {
            System.out.println("PREVENTED UNLOAD of " + event.getChunk());
            event.setCancelled(true);
        }
    }

    public static class GCCommands {
        private PerUserChunkLoader component;

        public GCCommands(PerUserChunkLoader component) {
            this.component = component;
        }

        @Command(aliases = {"smartunload"},
                 desc = "Unload chunks",
                 flags = "f",
                 min = 0, max = 0)
        @CommandPermissions("skcraft.perf.unload-chunks")
        public void unloadChunks(CommandContext context, CommandSender sender) {
            component.unloadChunks(sender);
        }

        @Command(aliases = {"mychunks"},
                 desc = "List chunks that are kept loaded for you",
                 flags = "p:",
                 min = 0, max = 0)
        public void myChunks(CommandContext context, CommandSender sender) throws CommandException {
            String name = sender.getName();

            // -p lets the user change the name
            if (context.hasFlag('p')) {
                if (!Rebar.getInstance().hasPermission(sender, "skcraft.perf.my-chunks.other")) {
                    throw new CommandPermissionsException();
                }

                name = context.getFlag('p');
            }

            List<LoadedChunk> chunks = component.listChunks(name);
            if (chunks.size() > 0) {
                sender.sendMessage(String.format("%sChunks loaded for %s (%d total):",
                        ChatColor.YELLOW, name, chunks.size()));
                for (LoadedChunk chunk : chunks) {
                    Chunk c = chunk.getChunk();

                    sender.sendMessage(String.format(
                            "%s%s: %d, %d (real: ~%d, %d) - '%s' priority %d",
                            ChatColor.GREEN, c.getWorld().getName(),
                            c.getX(), c.getZ(),
                            c.getX() * 16, c.getZ() * 16,
                            chunk.getName(),
                            chunk.getPriority()));
                }
            } else {
                sender.sendMessage(String.format(
                        "%sNo chunks are currently force loaded for '%s' using the " +
                        "[LoadChunk] mechanism.",
                        ChatColor.YELLOW, name));
            }
        }

        @Command(aliases = {"whohasloaded"},
                 desc = "List players with loaded chunks",
                 min = 0, max = 0)
        @CommandPermissions("skcraft.perf.unload-chunks")
        public void whoHasLoaded(CommandContext context, CommandSender sender) throws CommandException {
            List<String> names = component.listNamesWithChunks();
            if (names.size() > 0) {
                sender.sendMessage(String.format(
                        "%sUsers with force loaded chunks: %s",
                        ChatColor.YELLOW, StringUtil.joinString(names, ", ", 0)));
            } else {
                throw new CommandException("No one has force loaded chunks right now.");
            }
            sender.sendMessage(String.format(
                    "%s(Use /mychunks -p name to investigate.)",
                    ChatColor.GRAY));
        }
    }

    private static class LoadedChunk {

        private final Chunk chunk;
        private final String name;
        private final int priority;

        public LoadedChunk(Chunk chunk, Sign sign) {
            this.chunk = chunk;
            this.name = sign.getLine(2);
            int priority = 0;
            try {
                priority = Integer.parseInt(sign.getLine(3).replace("Priority ", ""));
            } catch (IndexOutOfBoundsException e) {
            }
            this.priority = priority;
        }

        public boolean isLoaded() {
            return chunk.isLoaded();
        }

        public Chunk getChunk() {
            return chunk;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

    }

}
