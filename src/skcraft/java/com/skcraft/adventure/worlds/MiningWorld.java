/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure.worlds;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

import com.sk89q.rebar.Rebar;
import com.skcraft.adventure.WorldFactory;

public class MiningWorld implements WorldFactory {

    @Override
    public World create(String name) {
        WorldCreator creator = new WorldCreator(name);
        creator.environment(Environment.NORMAL);
        creator.seed("xburrow touches himself".hashCode());
        return Rebar.getInstance().getServer().createWorld(creator);
    }

    @Override
    public void init(World world) {
    }

    @Override
    public String getId() {
        return "mining";
    }

}
