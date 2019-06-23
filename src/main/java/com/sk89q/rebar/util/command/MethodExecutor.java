/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.command.annotation.Command;
import com.sk89q.rebar.util.command.annotation.Parameters;
import com.skcraft.rebar.Actor;
import lombok.NonNull;
import lombok.extern.java.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

@Log
public class MethodExecutor implements CommandExecutor {

    private final Command command;
    private final Parameters params;
    private final Object object;
    private final Method method;

    public MethodExecutor(@NonNull Command command, Object object, @NonNull Method method) {
        this.command = command;
        this.object = object;
        this.method = method;
        this.params = method.getAnnotation(Parameters.class);
    }

    @Override
    public String getDescription(Actor actor) {
        return this.command.description();
    }

    @Override
    public boolean mayExecutePossibly(Actor actor) {
        return mayExecute(actor);
    }

    public boolean mayExecute(Actor actor) {
        String[] perms = command.permit();

        for (String perm : perms) {
            if (perm.equals("*")) {
                return true;
            } else if (Rebar.getInstance().hasPermission(actor, perm)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void execute(Actor actor, String[] args) throws CommandException {
        try {
            if (mayExecute(actor)) {
                CommandContext context = createContext(params, args);
                handleReturn(actor, args, method.invoke(object, provideArguments(actor, context)));
            } else {
                throw new CommandException("Sorry, you are unable to use this command.");
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof CommandException) {
                throw (CommandException) cause;
            } else {
                log.log(Level.WARNING, "Failed to execute command", e);
                throw new CommandException("Failed to execute command: " + e.getMessage());
            }
        }
    }

    protected void handleReturn(Actor actor, String[] args, Object value) throws CommandException {
    }

    protected Object[] provideArguments(Actor actor, CommandContext context) throws CommandException {
        return new Object[] { context, actor };
    }

    protected CommandContext createContext(Parameters params, String[] args) throws CommandException {
        if (params == null) {
            return new CommandContext(args);
        }

        final Set<Character> valueFlags = new HashSet<Character>();

        char[] flags = params.flags().toCharArray();
        Set<Character> newFlags = new HashSet<Character>();
        for (int i = 0; i < flags.length; ++i) {
            if (flags.length > i + 1 && flags[i + 1] == ':') {
                valueFlags.add(flags[i]);
                ++i;
            }
            newFlags.add(flags[i]);
        }

        CommandContext context = new CommandContext(args, valueFlags);

        if (context.argsLength() < params.min()) {
            throw new CommandException("Too few arguments");
        }

        if (params.max() != -1 && context.argsLength() > params.max()) {
            throw new CommandException("Too many arguments");
        }

        if (!params.anyFlags()) {
            for (char flag : context.getFlags()) {
                if (!newFlags.contains(flag)) {
                    throw new CommandException("Unknown flag: " + flag);
                }
            }
        }

        return context;
    }

    public static CommandGroup fromMethods(Object object) {
        SimpleCommandGroup group = new SimpleCommandGroup();
        for (Method method : object.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            Command command = method.getAnnotation(Command.class);
            if (command != null) {
                MethodExecutor executor = new MethodExecutor(command, object, method);
                String name = command.as().isEmpty() ? method.getName() : command.as();
                group.register(name, executor, Arrays.asList(command.aliases()));
            }
        }
        return group;
    }

}
