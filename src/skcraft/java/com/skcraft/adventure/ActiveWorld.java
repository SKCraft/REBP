/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure;

import org.bukkit.World;

public class ActiveWorld {
    
    private String id;
    private World world;
    private WorldFactory factory;
    
    public ActiveWorld(String id, World world, WorldFactory factory) {
        this.id = id;
        this.world = world;
        this.factory = factory;
    }
    
    public String getName() {
        return id;
    }
    
    public World getWorld() {
        return world;
    }
    
    public void setWorld(World world) {
        this.world = world;
    }
    
    public WorldFactory getFactory() {
        return factory;
    }

}
