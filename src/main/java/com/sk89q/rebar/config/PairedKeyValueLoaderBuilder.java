/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.Map.Entry;

/**
 * Build {@link KeyValueLoader}s and {@link KeyValueBuilder}s from a set of
 * different {@link Loader}s and {@link Builder}s for key and value.
 *
 * @author sk89q
 * @param <V1> key type
 * @param <V2> value type
 */
public abstract class PairedKeyValueLoaderBuilder<V1, V2> implements
        KeyValueLoader<V1, V2>, KeyValueBuilder<V1, V2> {

    /**
     * Build {@link KeyValueLoader}s and {@link KeyValueBuilder}s from a set of
     * different {@link Loader}s and {@link Builder}s for key and value.
     *
     * @param keyLoaderBuilder key loader builder
     * @param valueLoaderBuilder value loader builder
     * @param <K> key type
     * @param <V> value type
     * @return paired loader/builder for both keys and values
     */
    public static <K, V, E1 extends Loader<K> & Builder<K>, E2 extends Loader<V> & Builder<V>> PairedKeyValueLoaderBuilder<K, V> build(
            E1 keyLoaderBuilder, E2 valueLoaderBuilder) {
        return new PairedKeyValueLoaderImpl<K, V, E1, E2>(keyLoaderBuilder,
                valueLoaderBuilder);
    }

    private static class PairedKeyValueLoaderImpl<V1, V2, E1 extends Loader<V1> & Builder<V1>, E2 extends Loader<V2> & Builder<V2>>
            extends PairedKeyValueLoaderBuilder<V1, V2> {

        private final E1 keyLoaderBuilder;
        private final E2 valueLoaderBuilder;

        private PairedKeyValueLoaderImpl(E1 keyLoaderBuilder,
                E2 valueLoaderBuilder) {
            this.keyLoaderBuilder = keyLoaderBuilder;
            this.valueLoaderBuilder = valueLoaderBuilder;
        }

        @Override
        public Entry<Object, Object> write(V1 key, V2 value) {
            Object keyObject = keyLoaderBuilder.write(key);
            Object valueObject = valueLoaderBuilder.write(value);
            return new KeyValue<Object, Object>(keyObject, valueObject);
        }

        @Override
        public Entry<V1, V2> read(Object key, Object value) {
            V1 keyObject = keyLoaderBuilder.read(key);
            if (keyObject == null)
                return null;
            V2 valueObject = valueLoaderBuilder.read(value);
            if (valueObject == null)
                return null;
            return new KeyValue<V1, V2>(keyObject, valueObject);
        }

    }

}
