/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.google.inject.Provider;
import com.sk89q.rebar.util.command.ExecutionContext;
import com.skcraft.rebar.Actor;

public abstract class ArgumentProvider<T> implements Provider<T> {

    @Override
    public final T get() {
        ExecutionContext context = ExecutionContext.get();
        return get(context.getActor(), context.getCurrentParameter(), context.getArguments());
    }

    protected abstract T get(Actor actor, Parameter parameter, ArgumentStack args);

}
