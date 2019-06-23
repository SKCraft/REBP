/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * A criteria wrapper that inverts the condition.
 * 
 * @author sk89q
 * @param <T> underlying criteria
 */
public class InvertedCriteria<T extends Context> implements Criteria<T> {
    
    private final Criteria<T> criteria;
    
    public InvertedCriteria(Criteria<T> criteria) {
        this.criteria = criteria;
    }

    @Override
    public boolean matches(T context) {
        return !criteria.matches(context);
    }

}
