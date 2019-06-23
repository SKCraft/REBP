/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic.families;

import org.bukkit.block.Block;

import com.skcraft.reic.AbstractState;
import com.skcraft.reic.Family;
import com.skcraft.reic.State;

public class TripleInputSingleOutput implements Family {

    public State createState(Block sign) {
        return new State3ISO(sign);
    }
    
    public static class State3ISO extends AbstractState {
        
        private boolean[] input = new boolean[3];
        private boolean output = false;
        private boolean[] changed = new boolean[3];

        public State3ISO(Block sign) {
            super(sign);
        }

        public boolean update() {
            boolean[] newInput = new boolean[3];
            
            newInput[0] = isPowered(getBlock(), getFace0());
            newInput[1] = isPowered(getBlock(), getFace1());
            newInput[2] = isPowered(getBlock(), getFace2());

            output = isPoweredOutput(getSingleOutput());

            changed[0] = changed[0] || newInput[0] != input[0];
            changed[1] = changed[1] || newInput[1] != input[1];
            changed[2] = changed[2] || newInput[2] != input[2];
            
            input = newInput;
            
            return changed[0] || changed[1] || changed[2];
        }
        
        public void clearTriggered() {
            changed = new boolean[3];
        }

        public boolean in(int pin) {
            if (pin < 0 || pin >= numIn()) return false;
            return input[pin];
        }

        public boolean out(int pin) {
            return pin == 0 ? output : false;
        }

        public void out(int pin, boolean val) {
            setOutput(getSingleOutput(), val);
        }

        public boolean triggered(int pin) {
            if (pin < 0 || pin >= numIn()) return false;
            return changed[pin];
        }

        public int numIn() {
            return 3;
        }

        public int numOut() {
            return 1;
        }
        
    }

}
