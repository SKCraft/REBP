/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class DoubleLoaderBuilder implements Loader<Double>, Builder<Double> {

    @Override
    public Object write(Double value) {
        return value;
    }

    @Override
    public Double read(Object value) {
        return valueOf(value);
    }

    public static Double valueOf(Object o) {
        if (o == null) {
            return null;
        } else if (o instanceof Float) {
            return (double)(Float)o;
        } else if (o instanceof Double) {
            return (Double)o;
        } else if (o instanceof Byte) {
            return (double)(Byte)o;
        } else if (o instanceof Integer) {
            return (double)(Integer)o;
        } else if (o instanceof Long) {
            return (double)(Long)o;
        } else {
            return null;
        }
    }

}
