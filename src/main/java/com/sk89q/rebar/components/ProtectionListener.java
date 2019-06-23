/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

public abstract class ProtectionListener {
    
    public void onEvent(BlockInteractEvent event) {
    }

    public void onEvent(ItemUseEvent itemUseEvent) {
    }

    public void onEvent(EntityInteractEvent event) {
    }
    
    public void onEvent(GuestProtectedEvent event) {
    }

}
