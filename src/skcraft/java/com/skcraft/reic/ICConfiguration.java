/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public final class ICConfiguration {

    private String id;
    private ICFactory factory;
    private Family family;
    
    ICConfiguration(String id, ICFactory factory, Family family) {
        this.id = id;
        this.factory = factory;
        this.family = family;
    }

    public String getId() {
        return id;
    }

    public ICFactory getFactory() {
        return factory;
    }
    
    public Family getFamily() {
        return family;
    }
    
    public IC create(ReIC reIC, Block block, String[] lines) throws ICException {
        return getFactory().create(reIC, getFamily(), block, lines);
    }
    
    public IC create(ReIC reIC, Block block, String[] lines, Player player) throws ICException {
        return getFactory().create(reIC, getFamily(), block, lines, player);
    }
    
}
