/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.event;

import java.util.*;

public class EventManager {

    public static final short PRIORITY_CRITICAL = 2000;
    public static final short PRIORITY_HIGH = 1000;
    public static final short PRIORITY_NORMAL = 0;
    public static final short PRIORITY_LOW = -1000;
    public static final short PRIORITY_LOWEST = -2000;
    
    private static final ListenerRegistrationComparator comparator = new ListenerRegistrationComparator();
    private final Map<Class<?>, List<ListenerRegistration<?>>> registered;
    
    public EventManager() {
        registered = new HashMap<Class<?>, List<ListenerRegistration<?>>>();
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> void register(Class<? extends Event<T>> eventCls, T listener, short priority) {
        List<ListenerRegistration<?>> listeners = registered.get(eventCls);
        if (listeners == null) {
            listeners = new ArrayList<ListenerRegistration<?>>();
            registered.put(eventCls, listeners);
        }
        listeners.add(new ListenerRegistration(eventCls, listener, priority));
        Collections.sort(listeners, comparator);
    }
    
    @SuppressWarnings("unchecked")
    public <T> Event<T> dispatch(Event<T> event) {
        List<ListenerRegistration<?>> listeners = registered.get(event.getClass());
        if (listeners != null) {
            for (ListenerRegistration<?> listener : listeners) {
                event.call((T) listener.getListener()); // TODO Need to catch exceptions!
            }
        }
        
        return event;
    }
    
}