/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import java.util.List;

class RuleEntry {
    
    private final List<String> attachmentNames;
    private final Rule<?> rule;
    
    public RuleEntry(List<String> attachmentNames, Rule<?> rule) {
        this.attachmentNames = attachmentNames;
        this.rule = rule;
    }

    public List<String> getAttachmentNames() {
        return attachmentNames;
    }

    public Rule<?> getRule() {
        return rule;
    }

}
