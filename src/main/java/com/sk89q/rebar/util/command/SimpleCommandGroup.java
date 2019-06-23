/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.google.common.base.Joiner;
import com.sk89q.minecraft.util.commands.CommandException;
import com.skcraft.rebar.Actor;
import lombok.NonNull;

import java.util.*;

public class SimpleCommandGroup implements CommandGroup {

    private final Map<String, MappingEntry> byRoot = new HashMap<>();
    private final Map<String, MappingEntry> byRootAndAliases = new HashMap<>();

    public SimpleCommandGroup child(String name, CommandExecutor executor) {
        String[] names = name.split("\\|");
        List<String> aliases = Arrays.asList(names).subList(1, names.length);
        register(names[0], executor, aliases);
        return this;
    }

    public SimpleCommandGroup child(String name, CommandExecutor executor, List<String> aliases) {
        register(name, executor, aliases);
        return this;
    }

    @Override
    public void register(String name, CommandExecutor executor) {
        register(name, executor, Collections.<String>emptyList());
    }

    @Override
    public void register(@NonNull String name, @NonNull CommandExecutor executor, @NonNull List<String> aliases) {
        MappingEntry entry = new ImmutableMappingEntry(name, aliases, executor);
        byRoot.put(name, entry);
        byRootAndAliases.put(name, entry);
        for (String alias : aliases) {
            byRootAndAliases.put(name, entry);
        }
    }

    @Override
    public String getDescription(Actor actor) {
        List<String> permittedCommands = new ArrayList<>();
        for (MappingEntry entry : byRoot.values()) {
            if (entry.getExecutor().mayExecutePossibly(actor)) {
                permittedCommands.add(entry.getName());
            }
        }

        if (permittedCommands.size() > 0) {
            return "Child commands: " + Joiner.on(",").join(permittedCommands);
        } else {
            return "No child commands are available to you.";
        }
    }

    @Override
    public void execute(Actor actor, String[] args) throws CommandException {
        if (args.length == 1) { // No child command was specified
            throw new CommandException(getDescription(actor));
        } else {
            String childCmd = args[1].toLowerCase(); // Index 0 is this command
            String[] childArgs = Arrays.copyOfRange(args, 1, args.length);
            MappingEntry entry = byRootAndAliases.get(childCmd);

            if (entry != null) {
                entry.getExecutor().execute(actor, childArgs);
            } else {
                throw new CommandException("The child command '" + childCmd + "' doesn't exist.\n" + getDescription(actor));
            }
        }
    }

    @Override
    public boolean mayExecutePossibly(Actor actor) {
        for (MappingEntry entry : byRoot.values()) {
            if (entry.getExecutor().mayExecutePossibly(actor)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Iterator<MappingEntry> iterator() {
        return byRoot.values().iterator();
    }

    public static SimpleCommandGroup group() {
        return new SimpleCommandGroup();
    }

}
