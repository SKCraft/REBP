/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public interface State {
    
    Block getBlock();

    Sign getSign();

    Block getAttachedTo();

    boolean update();

    void clearTriggered();
    
    boolean in(int pin);
    
    boolean out(int pin);
    
    void out(int pin, boolean val);
    
    boolean passthrough(int pinIn);
    
    boolean passthrough(int pinIn, int pinOut);
    
    boolean triggered(int pin);

    int numIn();
    
    int numOut();
    
    boolean hasIn(int pin);
    
    boolean hasOut(int pin);
    
    void setNextTick(int ticks);
    
    void clearTick();
    
    int getNextTick();
    
    boolean tickCleared();
    
    void reset();
    
}
