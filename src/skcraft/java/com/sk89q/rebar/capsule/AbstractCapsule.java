/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import com.sk89q.rebar.capsule.binding.BindingGuard;
import lombok.Getter;

public abstract class AbstractCapsule implements Capsule {

    @Getter
    private final BindingGuard guard = new BindingGuard();

    @Override
    public final void initialize() {
        preBind();
    }

    @Override
    public final void shutdown() {
        guard.unbind();
        postUnbind();
    }

    protected void preBind() {
    }

    protected void postUnbind() {
    }

}
