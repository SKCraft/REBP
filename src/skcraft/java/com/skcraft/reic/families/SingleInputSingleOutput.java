/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.families;

import org.bukkit.block.Block;

import com.skcraft.reic.AbstractState;
import com.skcraft.reic.Family;
import com.skcraft.reic.State;

public class SingleInputSingleOutput implements Family {

    public State createState(Block sign) {
        return new StateSISO(sign);
    }
    
    public static class StateSISO extends AbstractState {
        
        private boolean input = false;
        private boolean output = false;
        private boolean changed = false;

        public StateSISO(Block sign) {
            super(sign);
        }

        public boolean update() {
            boolean lastInput = input;
            
            input = isPowered(getBlock(), getFace0()) 
                    || isPowered(getBlock(), getFace1())
                    || isPowered(getBlock(), getFace2());
            
            output = isPoweredOutput(getSingleOutput());
            
            return changed = (changed || lastInput != input);
        }
        
        public void clearTriggered() {
            changed = false;
        }

        public boolean in(int pin) {
            return pin == 0 ? input : false;
        }

        public boolean out(int pin) {
            return pin == 0 ? output : false;
        }

        public void out(int pin, boolean val) {
            setOutput(getSingleOutput(), val);
        }

        public boolean triggered(int pin) {
            return pin == 0 ? changed : false;
        }

        public int numIn() {
            return 1;
        }

        public int numOut() {
            return 1;
        }
        
    }

}
