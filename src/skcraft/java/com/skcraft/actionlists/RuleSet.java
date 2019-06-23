/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.LinkedList;
import java.util.List;

class RuleSet implements Attachment {
    
    private List<Rule<?>> rules = new LinkedList<Rule<?>>();

    @Override
    public void learn(Rule<?> rule) {
        rules.add(rule);
    }

    @Override
    public void forgetRules() {
        rules.clear();
    }
    
    public boolean hasRules() {
        return rules.size() > 0;
    }
    
    public List<Rule<?>> getRules() {
        return rules;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public boolean apply(Context context) {
        for (Rule rule : rules) {
            if (rule.matches(context)) {
                rule.apply(context);
            }
        }
        
        return context.isCancelled();
    }

}
