/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.ic.logic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.skcraft.reic.AbstractIC;
import com.skcraft.reic.AbstractICFactory;
import com.skcraft.reic.Family;
import com.skcraft.reic.IC;
import com.skcraft.reic.ICDocumentation;
import com.skcraft.reic.ICException;
import com.skcraft.reic.ReIC;
import com.skcraft.reic.State;

public class DelayIC extends AbstractIC {
    
    private int delay;
    
    public DelayIC(Block block, State state, int delay) {
        super(block, state);
        this.delay = delay;
    }
    
    public int getTriggerDelay() {
        return delay;
    }

    public void trigger() {
        getState().passthrough(0);
    }

    public void unload() {
    }
    
    public String getSummary() {
        return "Will delay the input to the output " + delay + " ticks.";
    }

    public static class DelayICFactory extends AbstractICFactory {
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
            
            return new DelayIC(sign, family.createState(sign), delay);
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Outputs the input state after the specified delay time.";
        }

        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Outputs the input state after the specified delay time. " +
                            "If the IC is triggered before the delay fires, then the delay is reset.")
            		.param("Delay (in ticks)")
            		.input("Input signal")
            		.output("Delayed signal");
        }
    }

}
