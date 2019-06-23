/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.inventory.Inventory;

public class AmplifierManager {
    
    private final List<Amplifier> amplifiers = new LinkedList<Amplifier>();
    
    public void register(Amplifier amplifier) {
        amplifiers.add(amplifier);
    }
    
    public Amplifier find(Inventory inven) {
        for (Amplifier amp : amplifiers) {
            if (amp.matches(inven)) {
                return amp;
            }
        }
        
        return null;
    }

}
