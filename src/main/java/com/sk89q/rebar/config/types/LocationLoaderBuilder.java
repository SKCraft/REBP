/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.Loader;
import com.sk89q.rebar.util.MapBuilder.ObjectMapBuilder;

public class LocationLoaderBuilder implements Loader<Location>, Builder<Location> {

    private final Server server;
    private final World defaultWorld;
    private final boolean precisionWorld;

    public LocationLoaderBuilder(Server server, boolean precisionWorld) {
        this.server = server;
        this.defaultWorld = null;
        this.precisionWorld = precisionWorld;
    }

    public LocationLoaderBuilder(World defaultWorld) {
        this.server = null;
        this.defaultWorld = defaultWorld;
        this.precisionWorld = false;
    }

    @Override
    public Object write(Location value) {
        ObjectMapBuilder builder = new ObjectMapBuilder();

        if (defaultWorld != null) {
            builder.put("world", value.getWorld().getName());

            if (precisionWorld) {
                builder.put("world-uuid-least", value.getWorld().getUID().getLeastSignificantBits());
                builder.put("world-uuid-most", value.getWorld().getUID().getMostSignificantBits());
            }
        }

        return builder
            .put("x", value.getX())
            .put("y", value.getY())
            .put("z", value.getZ())
            .map();
    }

    @Override
    public Location read(Object value) {
        ConfigurationNode node = new ConfigurationNode(value);
        Double x = node.getDouble("x");
        Double y = node.getDouble("y");
        Double z = node.getDouble("z");

        if (x == null || y == null || z == null) {
            return null;
        }

        World world = null;

        if (defaultWorld != null) {
            world = defaultWorld;
        } else {
            String worldName = node.getString("world");
            Long leastSigBits = node.getLong("world-uuid-least");
            Long mostSigBits = node.getLong("world-uuid-most");

            if (leastSigBits != null && mostSigBits != null) {
                world = server.getWorld(new UUID(mostSigBits, leastSigBits));
            } else {
                world = server.getWorld(worldName);
            }
        }

        if (world == null) {
            return null;
        }

        return new Location(world, x, y, z);
    }

}
