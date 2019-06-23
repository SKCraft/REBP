/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * A rule that may match a number of events and can be applied in
 * a situation. See {@link DefinedRule} for a rule implementation that
 * consists of constituent {@link Criteria}s and {@link Action}s.
 * 
 * @author sk89q
 * @param <T> implementation-specific context
 */
public interface Rule<T extends Context> {
    
    boolean matches(T context);
    
    void apply(T context);
    
}
