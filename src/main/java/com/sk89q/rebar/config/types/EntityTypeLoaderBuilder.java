/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import java.util.logging.Logger;

import org.bukkit.entity.EntityType;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class EntityTypeLoaderBuilder implements Loader<EntityType>, Builder<EntityType> {

    private Logger logger = Logger.getLogger(EntityTypeLoaderBuilder.class.getCanonicalName());

    @Override
    public EntityType read(Object value) {
        String stringValue = String.valueOf(value);
        try {
            EntityType type = EntityType.valueOf(stringValue);
            return type;
        } catch (IllegalArgumentException e) {
            logger.warning("EntityTypeResolver: Could not find entity type " + stringValue);
            return null;
        }
    }

    @Override
    public Object write(EntityType value) {
        return value.name();
    }

}