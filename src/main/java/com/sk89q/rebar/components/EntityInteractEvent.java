/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.sk89q.rebar.event.AbstractEvent;

public class EntityInteractEvent extends AbstractEvent<ProtectionListener> {

    private Entity victim;
    private Entity causer;

    public EntityInteractEvent(Entity victim) {
        this.victim = victim;
        this.causer = null;
    }

    public EntityInteractEvent(Entity victim, Entity causer) {
        this.victim = victim;
        this.causer = causer;
    }

    public Entity getVictim() {
        return victim;
    }

    public Entity getCauser() {
        return causer;
    }

    public boolean isPlayerCaused() {
        return causer instanceof Player
                && !((Player) causer).getName().startsWith("[");
    }

    public boolean isWorldCaused() {
        return causer == null
                || (isPlayerCaused() || ((Player) causer).getName().startsWith(
                        "["));
    }

    @Override
    public void call(ProtectionListener listener) {
        listener.onEvent(this);
    }

}
