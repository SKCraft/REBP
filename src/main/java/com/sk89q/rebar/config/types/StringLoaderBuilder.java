/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;

public class StringLoaderBuilder implements Loader<String>, Builder<String> {

    @Override
    public Object write(String value) {
        return String.valueOf(value);
    }

    @Override
    public String read(Object value) {
        return String.valueOf(value);
    }

}
