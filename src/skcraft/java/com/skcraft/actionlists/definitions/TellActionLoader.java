/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import static com.skcraft.actionlists.RuleEntryLoader.INLINE;

import com.skcraft.actionlists.ActionListsManager;
import com.skcraft.actionlists.DefinitionException;
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;

public class TellActionLoader extends AbstractNodeLoader<TellAction> {

    private final ActionListsManager manager;

    public TellActionLoader(ActionListsManager manager) {
        this.manager = manager;
    }

    @Override
    public TellAction read(ConfigurationNode node) throws DefinitionException {
        String message = node.contains(INLINE) ? node.getString(INLINE) : node.getString("message");

        TellAction action = new TellAction(message);
        action.setParser(manager.getParser());

        return action;
    }

}
