/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

/**
 * Build a configuration-ready object (that can easily be marshaled, such as a map, an
 * integer, a {@link ConfigurationNode}, or etc.) given a raw meaningful value.
 *
 * @author sk89q
 * @param <V> type of object
 */
public interface Builder<V> {

    /**
     * Returns a configuration-ready object from a raw meaningful value.
     *
     * @param value raw meaningful value
     * @return configuration-ready object, possibly null
     * @throws LoaderBuilderException an unchecked exception on a fatal error
     */
    Object write(V value) throws LoaderBuilderException;

}
