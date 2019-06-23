/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.service;

/**
 * A registered service provider.
 *
 * @author sk89q
 * @param <T> Service
 */
public class RegisteredServiceProvider<T> implements Comparable<RegisteredServiceProvider<?>> {

    private Class<T> service;
    private T provider;
    private short priority;

    public RegisteredServiceProvider(Class<T> service, T provider, short priority) {
        this.service = service;
        this.provider = provider;
        this.priority = priority;
    }

    public Class<T> getService() {
        return service;
    }

    public T getProvider() {
        return provider;
    }

    public short getPriority() {
        return priority;
    }

    @Override
    public int compareTo(RegisteredServiceProvider<?> other) {
        if (priority == other.getPriority()) {
            return 0;
        } else {
            return priority < other.getPriority() ? 1 : -1;
        }
    }
}
