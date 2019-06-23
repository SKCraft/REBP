/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;

public interface Family {
    
    State createState(Block sign);

}
