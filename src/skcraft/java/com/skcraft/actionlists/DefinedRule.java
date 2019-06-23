/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefinedRule<T extends Context> implements Rule<T> {
    
    private static Logger logger = Logger.getLogger(Rule.class.getCanonicalName());

    private final List<Criteria<T>> criterion = new LinkedList<Criteria<T>>();
    private final List<Action<T>> actions = new LinkedList<Action<T>>();
    
    public void add(Criteria<T> criteria) {
        criterion.add(criteria);
    }
    
    public void add(Action<T> action) {
        actions.add(action);
    }
    
    public boolean hasCriterion() {
        return criterion.size() > 0;
    }
    
    public boolean hasActions() {
        return actions.size() > 0;
    }
    
    @Override
    public boolean matches(T context) {
        for (Criteria<T> criteria : criterion) {
            if (!criteria.matches(context)) {
                context.setMatches(false);
                return false;
            }
        }

        context.setMatches(true);
        return true;
    }
    
    @Override
    public void apply(T context) {
        for (Action<T> action : actions) {
            try {
                action.apply(context);
            } catch (Throwable t) {
                logger.log(Level.WARNING, "Failed to apply action of rule", t);
            }
        }
    }
    
}
