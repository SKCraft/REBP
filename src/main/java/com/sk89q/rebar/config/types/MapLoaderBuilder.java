/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import java.util.Map;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

class MapLoaderBuilder implements Loader<Map<Object, Object>>, Builder<Map<Object, Object>> {

    @Override
    public Object write(Map<Object, Object> value) {
        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<Object, Object> read(Object value) {
        if (value instanceof Map) {
            return (Map<Object, Object>) value;
        } else {
            return null;
        }
    }

}
