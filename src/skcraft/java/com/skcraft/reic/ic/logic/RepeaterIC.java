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

public class RepeaterIC extends AbstractIC {
    
    public RepeaterIC(Block block, State state) {
        super(block, state);
    }

    public void trigger() {
        getState().passthrough(0);
    }

    public void unload() {
    }

    public static class RepeaterICFactory extends AbstractICFactory {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            expectNoArgs(lines);
            return new RepeaterIC(sign, family.createState(sign));
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Outputs the input state 2 ticks after.";
        }
        
        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Outputs the input state 2 ticks after.")
                    .input("Input value")
                    .output("Outputted value");
        }
    }

}
