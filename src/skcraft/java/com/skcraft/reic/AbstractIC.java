/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;

public abstract class AbstractIC implements IC {
    
    private Block block;
    private State state;
    
    public AbstractIC(Block block, State state) {
        this.block = block;
        this.state = state;
    }
    
    public Block getBlock() {
        return block;
    }

    public State getState() {
        return state;
    }

    public void initialize() {
    }

    public void tick() {
    }
    
    public int getTriggerDelay() {
        return 1;
    }
    
    public String getSummary() {
        return null;
    }
    
    public String getDebugInfo() {
        return null;
    }

}
