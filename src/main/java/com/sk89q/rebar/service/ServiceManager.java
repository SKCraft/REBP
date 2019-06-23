/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple services manager.
 *
 * @author sk89q
 */
public class ServiceManager {

    public static final short PRIORITY_CRITICAL = 2000;
    public static final short PRIORITY_HIGH = 1000;
    public static final short PRIORITY_NORMAL = 0;
    public static final short PRIORITY_LOW = -1000;
    public static final short PRIORITY_LOWEST = -2000;

    /**
     * Map of providers.
     */
    private final Map<Class<?>, List<RegisteredServiceProvider<?>>> providers = new HashMap<Class<?>, List<RegisteredServiceProvider<?>>>();

    /**
     * Register a provider of a service.
     *
     * @param <T> Provider
     * @param service service class
     * @param provider provider to register
     * @param priority priority of the provider
     */
    public <T> void register(Class<T> service, T provider, short priority) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

            if (registered == null) {
                registered = new ArrayList<RegisteredServiceProvider<?>>();
                providers.put(service, registered);
            }

            registered.add(new RegisteredServiceProvider<T>(service, provider, priority));

            // Make sure that providers are in the right order in order
            // for priorities to work correctly
            Collections.sort(registered);
        }
    }

    /**
     * Queries for a provider. This may return if no provider has been
     * registered for a service. The highest priority provider is returned.
     *
     * @param <T> The service interface
     * @param service The service interface
     * @return provider or null
     */
    @SuppressWarnings("unchecked")
    public <T> T load(Class<T> service) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

            if (registered == null) {
                return null;
            }

            // This should not be null!
            return (T) registered.get(0).getProvider();
        }
    }

    /**
     * Queries for a provider registration. This may return if no provider
     * has been registered for a service.
     *
     * @param <T> The service interface
     * @param service The service interface
     * @return provider registration or null
     */
    @SuppressWarnings("unchecked")
    public <T> RegisteredServiceProvider<T> getRegistration(Class<T> service) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

            if (registered == null) {
                return null;
            }

            // This should not be null!
            return (RegisteredServiceProvider<T>) registered.get(0);
        }
    }

    /**
     * Get registrations of providers for a service. The returned list is
     * unmodifiable.
     *
     * @param <T> The service interface
     * @param service The service interface
     * @return list of registrations
     */
    @SuppressWarnings("unchecked")
    public <T> Collection<RegisteredServiceProvider<T>> getRegistrations(Class<T> service) {
        synchronized (providers) {
            List<RegisteredServiceProvider<?>> registered = providers.get(service);

            if (registered == null) {
                return Collections.unmodifiableList(new ArrayList<RegisteredServiceProvider<T>>());
            }

            List<RegisteredServiceProvider<T>> ret = new ArrayList<RegisteredServiceProvider<T>>();

            for (RegisteredServiceProvider<?> provider : registered) {
                ret.add((RegisteredServiceProvider<T>) provider);
            }

            return Collections.unmodifiableList(ret);
        }
    }

    /**
     * Get a list of known services. A service is known if it has registered
     * providers for it.
     *
     * @return list of known services
     */
    public Collection<Class<?>> getKnownServices() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    /**
     * Returns whether a provider has been registered for a service. Do not
     * check this first only to call <code>load(service)</code> later, as that
     * would be a non-thread safe situation.
     *
     * @param <T> service
     * @param service service to check
     * @return whether there has been a registered provider
     */
    public <T> boolean isProvidedFor(Class<T> service) {
        return getRegistration(service) != null;
    }
}
