/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.Loader;
import com.sk89q.rebar.util.MapBuilder.ObjectMapBuilder;
import com.sk89q.worldedit.Vector2D;

public class Vector2dLoaderBuilder implements Loader<Vector2D>, Builder<Vector2D> {

    @Override
    public Object write(Vector2D value) {
        return new ObjectMapBuilder()
            .put("x", value.getX())
            .put("z", value.getZ())
            .map();
    }

    @Override
    public Vector2D read(Object value) {
        ConfigurationNode node = new ConfigurationNode(value);
        Double x = node.getDouble("x");
        Double z = node.getDouble("z");

        if (x == null || z == null) {
            return null;
        }

        return new Vector2D(x, z);
    }

}
