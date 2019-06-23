/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.redispense;

import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;
import org.bukkit.util.Vector;

public interface Amplifier {
 
    boolean matches(Inventory inven);
    boolean activate(Block block, Dispenser dispenser, Inventory inven, Vector vel);
    
}
