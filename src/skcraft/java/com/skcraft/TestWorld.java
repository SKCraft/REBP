/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;

public class TestWorld extends AbstractComponent {

    @Override
    public void initialize() {
        WorldCreator worldCreator = new WorldCreator("testworld");
        worldCreator.generator(new TestWorldGenerator(false));
        worldCreator.environment(Environment.NORMAL);
        Rebar.getInstance().getServer().createWorld(worldCreator);
    }

    @Override
    public void shutdown() {
    }

}
