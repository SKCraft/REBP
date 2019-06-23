/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.entity.Player;

import com.sk89q.rebar.event.AbstractEvent;

public class GuestProtectedEvent extends AbstractEvent<ProtectionListener> {

    private Player player;
    private String message = "You are not allowed to do that.";

    public GuestProtectedEvent(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public void call(ProtectionListener listener) {
        listener.onEvent(this);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
