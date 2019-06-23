/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class IntegerLoaderBuilder implements Loader<Integer>, Builder<Integer> {

    @Override
    public Object write(Integer value) {
        return value;
    }

    @Override
    public Integer read(Object value) {
        return valueOf(value);
    }

    public static Integer valueOf(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Byte) {
            return (int)(Byte)o;
        } else if (o instanceof Integer) {
            return (Integer)o;
        } else if (o instanceof Double) {
            return (int)(double)(Double)o;
        } else if (o instanceof Float) {
            return (int)(float)(Float)o;
        } else if (o instanceof Long) {
            return (int)(long)(Long)o;
        } else {
            return null;
        }
    }

}
