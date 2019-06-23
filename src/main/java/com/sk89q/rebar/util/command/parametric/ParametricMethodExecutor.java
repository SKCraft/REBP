/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command.parametric;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.command.CommandGroup;
import com.sk89q.rebar.util.command.MethodExecutor;
import com.sk89q.rebar.util.command.SimpleCommandGroup;
import com.sk89q.rebar.util.command.annotation.Command;
import com.sk89q.rebar.util.command.annotation.Parameters;
import com.sk89q.rebar.util.command.annotation.Respond;
import com.skcraft.rebar.Actor;
import lombok.NonNull;
import lombok.extern.java.Log;
import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Log
public class ParametricMethodExecutor extends MethodExecutor {

    private final Parameter[] parameters;
    private final ParameterProvider provider;
    private final Respond respondMessage;
    private final Set<Character> valueFlags = new HashSet<>();

    public ParametricMethodExecutor(@NonNull Command command, Object object,
                                    @NonNull Method method, @NonNull ParameterProvider provider) {
        super(command, object, method);
        this.provider = provider;
        this.parameters = ParameterProvider.parseParameters(method);
        this.respondMessage = method.getAnnotation(Respond.class);

        for (Parameter parameter : parameters) {
            Character flag = parameter.getFlag();
            if (flag != null && parameter.isValueFlag()) {
                valueFlags.add(flag);
            }
        }
    }

    @Override
    protected Object[] provideArguments(Actor actor, CommandContext context) throws CommandException {
        return provider.provide(actor, context, parameters);
    }

    @Override
    protected CommandContext createContext(Parameters params, String[] args) throws CommandException {
        return new CommandContext(args, valueFlags);
    }

    @Override
    protected void handleReturn(Actor actor, String[] args, Object value) throws CommandException {
        if (respondMessage != null) {
            if (value == null || (value instanceof Boolean && !(Boolean) value)) {
                if (respondMessage.asException()) {
                    throw new CommandException(respondMessage.otherwise());
                } else {
                    actor.message((respondMessage.format() ? ChatColor.RED : "") + respondMessage.otherwise());
                }
            } else {
                actor.message((respondMessage.format() ? ChatColor.YELLOW : "") + respondMessage.with());
            }
        }
    }

    public static CommandGroup fromMethods(Object object, ParameterProvider provider) {
        SimpleCommandGroup group = new SimpleCommandGroup();
        for (Method method : object.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            Command command = method.getAnnotation(Command.class);
            if (command != null) {
                ParametricMethodExecutor executor = new ParametricMethodExecutor(command, object, method, provider);
                String name = command.as().isEmpty() ? method.getName() : command.as();
                group.register(name, executor, Arrays.asList(command.aliases()));
            }
        }
        return group;
    }

}
