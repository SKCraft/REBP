/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.event;

public class ListenerRegistration<T> {

    private Class<? extends Event<T>> eventCls;
    private T listener;
    private short priority;
    
    public ListenerRegistration(Class<? extends Event<T>> eventCls, T listener, short priority) {
        this.eventCls = eventCls;
        this.listener = listener;
        this.priority = priority;
    }
    
    public void dispatch(Event<T> event) {
        event.call(listener);
    }

    public Class<? extends Event<T>> getEventClass() {
        return eventCls;
    }

    public T getListener() {
        return listener;
    }

    public short getPriority() {
        return priority;
    }
    
}
