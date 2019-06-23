/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.sk89q.rebar.components.sessions.Managed;
import com.sk89q.rebar.components.sessions.ProfileFactory;

public class MagicWandSession implements Managed {

    private WandActor actor;
    private int heldId;

    public MagicWandSession() {
    }

    @Override
    public void destroy() {
        if (actor != null) {
            actor.destroy();
        }
    }

    public boolean isHeld(ItemStack item) {
        return heldId == item.getTypeId();
    }

    public void setHeld(ItemStack item) {
        this.heldId = item.getTypeId();
    }

    public WandActor getActor() {
        return actor;
    }

    public void setActor(WandActor actor) {
        if (actor != null) {
            actor.destroy();
        }

        this.actor = actor;
    }

    public static class Factory implements ProfileFactory<MagicWandSession> {

        @Override
        public Class<MagicWandSession> getProfileClass() {
            return MagicWandSession.class;
        }

        @Override
        public boolean shouldPersist() {
            return true;
        }

        @Override
        public MagicWandSession create(Player player) {
            return new MagicWandSession();
        }

    }

}
