/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface ICFactory {
    
    IC create(ReIC reic, Family family, Block sign, String[] lines) throws ICException;
    
    IC create(ReIC reic, Family family, Block sign, String[] lines, Player player) throws ICException;
    
    boolean canCreate(Player player);
    
    String getDescription();
    
    ICDocumentation getDocumentation();

}
