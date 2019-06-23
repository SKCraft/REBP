/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import com.sk89q.rebar.util.WorldBlockCoord;

public class Tracker implements Runnable {

    private final static Logger logger = Logger.getLogger(Tracker.class.getCanonicalName());
    private final Map<WorldBlockCoord, IC> tracked;
    private final Map<WorldBlockCoord, QueueEntry> triggerQueue;
    private final Map<WorldBlockCoord, QueueEntry> triggerQueuePending;
    private final Map<WorldBlockCoord, QueueEntry> thinkQueue;
    private final Set<IC> needsUnload = new HashSet<IC>();
    private final Map<WorldBlockCoord, IC> needsProcess;
    private long currentTick = 0;
    private boolean ticking = false;
    
    public Tracker() {
        tracked = new HashMap<WorldBlockCoord, IC>();
        triggerQueue = new LinkedHashMap<WorldBlockCoord, QueueEntry>(20);
        triggerQueuePending = new LinkedHashMap<WorldBlockCoord, QueueEntry>(20);
        thinkQueue = new LinkedHashMap<WorldBlockCoord, QueueEntry>(20);
        needsProcess = new LinkedHashMap<WorldBlockCoord, IC>(20);
    }
    
    private void queueTrigger(Block block, IC ic, int delay) {
        if (ticking) {
            triggerQueuePending.put(new WorldBlockCoord(block), new QueueEntry(ic, currentTick + delay));
        } else {
            triggerQueue.put(new WorldBlockCoord(block), new QueueEntry(ic, currentTick + delay));
        }
    }
    
    private void tick() {
        ticking = true;
        currentTick++;

        Iterator<Map.Entry<WorldBlockCoord, QueueEntry>> it;
        
        // Trigger queue
        for (it = triggerQueue.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<WorldBlockCoord, QueueEntry> entry = it.next();
            QueueEntry queueEntry = entry.getValue();
            if (queueEntry.getTime() > currentTick) continue;
            
            IC ic = queueEntry.getIC();
            
            if (!isLikelyIC(entry.getKey())) {
                needsUnload.add(ic);
                continue;
            }
            
            try {
                ic.trigger();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            ic.getState().clearTriggered();
            
            needsProcess.put(entry.getKey(), ic);
        }

        if (needsProcess.size() > 0) {
            for (Map.Entry<WorldBlockCoord, IC> entry : needsProcess.entrySet()) {
                triggerQueue.remove(entry.getKey());
                processState(entry.getKey(), entry.getValue());
            }
            needsProcess.clear();
        }

        if (triggerQueuePending.size() > 0) {
            for (Map.Entry<WorldBlockCoord, QueueEntry> entry : triggerQueuePending.entrySet()) {
                triggerQueue.put(entry.getKey(), entry.getValue());
            }
            triggerQueuePending.clear();
        }
        
        ticking = false;
        
        // Think queue
        for (it = thinkQueue.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<WorldBlockCoord, QueueEntry> entry = it.next();
            QueueEntry queueEntry = entry.getValue();
            if (queueEntry.getTime() > currentTick) continue;
            
            IC ic = queueEntry.getIC();
            
            if (!isLikelyIC(entry.getKey())) {
                needsUnload.add(ic);
                continue;
            }
            
            try {
                ic.getState().update();
                ic.tick();
            } catch (Throwable t) {
                t.printStackTrace();
            }
            
            needsProcess.put(entry.getKey(), ic);
        }

        if (needsProcess.size() > 0) {
            for (Map.Entry<WorldBlockCoord, IC> entry : needsProcess.entrySet()) {
                triggerQueue.remove(entry.getKey());
                processState(entry.getKey(), entry.getValue());
            }
            needsProcess.clear();
        }
        
        // Unload
        if (needsUnload.size() > 0) {
            for (IC ic : needsUnload) {
                safeUnload(ic);
            }
            
            needsUnload.clear();
        }
    }
    
    public IC get(Block block) {
        return tracked.get(new WorldBlockCoord(block));
    }
    
    public void register(Block block, IC ic) {
        remove(block);
        WorldBlockCoord coord = new WorldBlockCoord(block);
        tracked.put(coord, ic);
        ic.getState().update();
        try {
            ic.initialize();
            processState(coord, ic);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void physicsUpdate(Block block, IC ic) {
        State state = ic.getState();
        if (state.update()) {
            queueTrigger(block, ic, ic.getTriggerDelay());
        }
    }
    
    public void remove(Block block) {
        WorldBlockCoord coord = new WorldBlockCoord(block);
        IC ic = tracked.remove(coord);
        if (ic != null) {
            triggerQueue.remove(coord);
            thinkQueue.remove(coord);
            triggerQueuePending.remove(coord);
            safeUnload(ic);
        }
    }
    
    private void processState(WorldBlockCoord coord, IC ic) {
        State state = ic.getState();
        
        int thinkDelay = state.getNextTick();
        
        if (thinkDelay > -1) {
            thinkQueue.put(coord, new QueueEntry(ic, currentTick + thinkDelay));
        } else if (state.tickCleared()) {
            thinkQueue.remove(coord);
        }
        
        state.reset();
    }
    
    private void safeUnload(IC ic) {
        try {
            ic.unload();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void removeChunk(Chunk chunk) {
        Iterator<Map.Entry<WorldBlockCoord, IC>> it = tracked.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<WorldBlockCoord, IC> entry = it.next();

            int chunkX = entry.getKey().getX() / 16;
            int chunkZ = entry.getKey().getZ() / 16;
            if (entry.getKey().getWorld().equals(chunk.getWorld())
                    && chunkX == chunk.getX() && chunkZ == chunk.getZ()) {
                safeUnload(entry.getValue());
                it.remove();
            }
        }
    }
    
    private static boolean isLikelyIC(WorldBlockCoord worldBlockCoord) {
        return worldBlockCoord.getBlock().getType() == Material.WALL_SIGN;
    }
    
    public static String parseICHeader(String text) {
        if (text.length() <= 4) return "";
        return text.substring(3, text.length() - 1);
    }

    public static boolean isICHeader(String text) {
        return text.startsWith(ChatColor.DARK_AQUA + "{") && text.endsWith("}");
    }

    public static String getICHeader(String id) {
        return ChatColor.DARK_AQUA + "{" + id + "}";
    }
    
    public void run() {
        try {
            tick();
        } catch (Throwable t) {
            logger.log(Level.WARNING, "ReIC tracker encountered an exception during a tick", t);
        }
    }

    private static class QueueEntry {
        private IC ic;
        private long time;
        
        public QueueEntry(IC ic, long time) {
            this.ic = ic;
            this.time = time;
        }
        
        public IC getIC() {
            return ic;
        }
        
        public long getTime() {
            return time;
        }
    }
    
    
}
