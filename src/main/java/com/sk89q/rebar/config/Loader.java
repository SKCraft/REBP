/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

/**
 * Loads a meaningful value from a object.
 *
 * @see AbstractNodeLoader
 * @param <V> type of object
 */
public interface Loader<V> {

    /**
     * Given the value, which could be any object or even null, return a meaningful
     * value or null. If null is returned, the value may or may not be discarded
     * depending on the calling method.
     *
     * @param value raw value
     * @return meaningful value, or null
     * @throws LoaderBuilderException an unchecked exception on a fatal error
     */
    V read(Object value) throws LoaderBuilderException;

}
