/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class BooleanLoaderBuilder implements Loader<Boolean>, Builder<Boolean> {

    @Override
    public Object write(Boolean value) {
        return value;
    }

    @Override
    public Boolean read(Object value) {
        return valueOf(value);
    }

    public static Boolean valueOf(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Boolean) {
            return (Boolean)o;
        } else {
            return null;
        }
    }

}
