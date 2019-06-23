/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Builds a key/value pair that can loads meaningful objects from raw values.
 *
 * @see Loader
 * @author sk89q
 * @param <K> type of object for the key
 * @param <V> type of object for the value
 */
public interface KeyValueLoader<K, V> {

    /**
     * See {@link Loader} for the general idea.
     *
     * @param key key
     * @param value value
     * @return entry containing
     * @see KeyValue implementation of a {@link Entry}
     * @throws LoaderBuilderException
     */
    Map.Entry<K, V> read(Object key, Object value) throws LoaderBuilderException;

}
