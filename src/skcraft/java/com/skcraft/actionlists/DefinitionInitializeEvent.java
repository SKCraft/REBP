/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DefinitionInitializeEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    
    private final ActionListsManager actionListsManager;
    
    public DefinitionInitializeEvent(ActionListsManager actionListsManager) {
        this.actionListsManager = actionListsManager;
    }
    
    public ActionListsManager getManager() {
        return actionListsManager;
    }
    
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
