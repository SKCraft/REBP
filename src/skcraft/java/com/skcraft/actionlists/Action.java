/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * An action that will be performed if the conditions are matched of the rule.
 * 
 * @author sk89q
 * @param <T> implementation-specific context object
 */
public interface Action<T extends Context> {
    
    /**
     * Apply the action.
     * 
     * @param context implementation-specific context object
     */
    void apply(T context);
    
}
