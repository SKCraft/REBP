/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.logic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.skcraft.reic.AbstractIC;
import com.skcraft.reic.AbstractICFactory;
import com.skcraft.reic.CreatedOnChunkLoad;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.State;

public class ClockIC extends AbstractIC {
    
    private int delay;
    
    public ClockIC(Block block, State state, int delay) {
        super(block, state);
        this.delay = delay;
    }
    
    public int getTriggerDelay() {
        return delay;
    }

    public void initialize() {
        trigger();
        getState().clearTriggered();
    }

    public void trigger() {
        State state = getState();
        
        if (state.in(0)) {
            state.setNextTick(delay);
        } else {
            state.clearTick();
        }
    }
    
    public void tick() {
        State state = getState();
        state.out(0, !state.out(0));
        state.setNextTick(delay);
    }

    public void unload() {
    }
    
    public String getSummary() {
        return "Will toggle every " + delay + " ticks.";
    }

    public static class ClockICFactory extends AbstractICFactory implements CreatedOnChunkLoad {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            int delay = 0;
            
            try {
                delay = Integer.parseInt(lines[1]);
                if (delay < 2) {
                    throw new ICException("The minimum delay is 2.");
                }
            } catch (NumberFormatException e) {
                throw new ICException("The delay (line 2) should be an integer.");
            }
            
            expectNoArg(lines, 2);
            expectNoArg(lines, 3);
            
            return new ClockIC(sign, family.createState(sign), delay);
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Toggles the output state at a certain specified interval.";
        }

        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Toggles the output state at a certain specified interval.")
                    .param("Interval (in ticks)")
                    .input("HIGH to enable")
                    .output("Clock signal");
        }
    }

}
