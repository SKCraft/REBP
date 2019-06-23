/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

/**
 * An object that can learn rules and possibly implement them. Attachments can be
 * analogous to events that occur in the game. An attachment, for example, would be
 * a block break event.
 *
 * @author sk89q
 */
public interface Attachment {

    /**
     * Learn a rule.
     *
     * @param rule rule
     */
    public void learn(Rule<?> rule);

    /**
     * Forget all previously learned rules.
     */
    public void forgetRules();

}
