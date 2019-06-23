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

public class AndIC extends AbstractIC {
    
    public AndIC(Block block, State state) {
        super(block, state);
    }

    public void trigger() {
        State state = getState();
        boolean val = true;
        for (int i = 0; i < state.numIn(); i++) {
            if (!state.in(i)) {
                val = false;
                break;
            }
        }
        state.out(0, val);
    }

    public void unload() {
    }

    public static class AndICFactory extends AbstractICFactory {
        public IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException {
            expectNoArgs(lines);
            return new AndIC(sign, family.createState(sign));
        }

        public boolean canCreate(Player player) {
            return true;
        }

        public String getDescription() {
            return "Outputs the AND result of all inputs.";
        }

        public ICDocumentation getDocumentation() {
            return new ICDocumentation()
                    .summary("Outputs the AND result of all inputs.")
                    .inputs("ANDed values")
                    .output("Result of AND");
        }
    }

}
