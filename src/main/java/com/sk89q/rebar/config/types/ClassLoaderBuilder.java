/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import java.util.logging.Logger;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class ClassLoaderBuilder implements Loader<Class<?>>, Builder<Class<?>> {

    private Logger logger = Logger.getLogger(ClassLoaderBuilder.class.getCanonicalName());

    @Override
    public Class<?> read(Object value) {
        String stringValue = String.valueOf(value);
        try {
            return Class.forName(stringValue);
        } catch (ClassNotFoundException e) {
            logger.warning("ClassResolver: Could not find class " + stringValue);
            return null;
        }
    }

    @Override
    public Object write(Class<?> value) {
        return value.getCanonicalName();
    }

}