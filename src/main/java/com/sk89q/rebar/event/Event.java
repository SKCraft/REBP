/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.event;

public interface Event<T> {
    
    public void call(T listener);
    
    public void uncancel();
    
    public void cancel();
    
    public boolean isCancelled();
    
}
