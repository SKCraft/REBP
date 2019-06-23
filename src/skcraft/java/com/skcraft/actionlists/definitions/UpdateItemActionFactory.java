/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import com.skcraft.actionlists.ActionListsManager;
import com.skcraft.actionlists.DefinitionException;
import com.skcraft.actionlists.SubjectResolver;
import com.sk89q.rebar.config.AbstractNodeLoader;
import com.sk89q.rebar.config.ConfigurationNode;

public class UpdateItemActionFactory extends AbstractNodeLoader<UpdateItemAction> {

    private final ActionListsManager manager;

    public UpdateItemActionFactory(ActionListsManager manager) {
        this.manager = manager;
    }

    @Override
    public UpdateItemAction read(ConfigurationNode node) throws DefinitionException {
        SubjectResolver<ItemStackSlot> resolver = manager.getSubjectResolvers()
                .getResolver(ItemStackSlot.class, node.getString("of", "held"));

        boolean destroy = node.getBoolean("destroy", false);
        short newData = (short) node.getInt("set-data", -1);

        UpdateItemAction action = new UpdateItemAction(resolver);
        action.setDestroy(destroy);
        action.setNewData(newData);

        return action;
    }

}
