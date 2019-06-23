/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

public class DummyBuilder<V> implements Loader<V>, Builder<V> {

    private final Loader<V> loader;

    public DummyBuilder(Loader<V> loader) {
        this.loader = loader;
    }

    @Override
    public Object write(V value) {
        return null;
    }

    @Override
    public V read(Object value) {
        return loader.read(value);
    }

}
