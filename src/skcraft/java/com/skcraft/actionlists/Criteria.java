/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * A criteria that is evaluated to decide whether the rule should undergo
 * further processing.
 * 
 * @author sk89q
 * @param <T> an implementation-specific context
 */
public interface Criteria<T extends Context> {

    /**
     * Returns whether the current event / context matches this criteria.
     * 
     * @param context context to verify
     * @return true if it matches
     */
    boolean matches(T context);

}
