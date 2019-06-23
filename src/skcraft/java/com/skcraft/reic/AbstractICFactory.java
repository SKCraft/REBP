/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class AbstractICFactory implements ICFactory {
    
    public IC create(ReIC reic, Family family, Block sign, String[] lines, Player player) throws ICException {
        return create(reic, family, sign, lines);
    }

    public void expectNoArg(String[] lines, int line) throws ICException {
        if (lines[line].length() > 0) {
            throw new ICException("Expected sign line " + (line + 1) + " (1..4) to be blank.");
        }
    }
    
    public void expectNoArgs(String[] lines) throws ICException {
        expectNoArg(lines, 1);
        expectNoArg(lines, 2);
        expectNoArg(lines, 3);
    }
}
