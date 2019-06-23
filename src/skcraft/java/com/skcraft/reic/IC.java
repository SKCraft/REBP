/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;

public interface IC {
    
    Block getBlock();
    
    State getState();

    void initialize();
    
    void trigger();
    
    void tick();

    void unload();
    
    int getTriggerDelay();
    
    String getSummary();

    String getDebugInfo();

}
