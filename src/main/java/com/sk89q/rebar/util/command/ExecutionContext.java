/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.rebar.util.command.parametric.ArgumentStack;
import com.skcraft.rebar.Actor;
import lombok.Data;

import com.sk89q.rebar.util.command.parametric.Parameter;

@Data
public final class ExecutionContext implements AutoCloseable {

    private final static ThreadLocal<ExecutionContext> local = new ThreadLocal<>();
    private Actor actor;
    private CommandContext context;
    private ArgumentStack arguments;
    private Parameter currentParameter;

    private ExecutionContext() {
        set(this);
    }

    @Override
    public void close() {
        set(null);
    }

    public static ExecutionContext get() {
        return local.get();
    }

    private static void set(ExecutionContext value) {
        local.set(value);
    }

    public static ExecutionContext createContext() {
        return new ExecutionContext();
    }

}
