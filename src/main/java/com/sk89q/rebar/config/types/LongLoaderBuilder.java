/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class LongLoaderBuilder implements Loader<Long>, Builder<Long> {

    @Override
    public Object write(Long value) {
        return value;
    }

    @Override
    public Long read(Object value) {
        return valueOf(value);
    }

    public static Long valueOf(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Byte) {
            return (long)(Byte)o;
        } else if (o instanceof Integer) {
            return (long)(Integer)o;
        } else if (o instanceof Double) {
            return (long)(double)(Double)o;
        } else if (o instanceof Float) {
            return (long)(float)(Float)o;
        } else if (o instanceof Long) {
            return (long)(Long)o;
        } else {
            return null;
        }
    }

}
