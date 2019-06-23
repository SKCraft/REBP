/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ReflectionUtil;
import lombok.extern.java.Log;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Log
public class BukkitCommandManager implements CommandManager {

    private final Set<String> registered = new HashSet<>();
    private SimpleCommandMap commandMap;
    private Map<String, Command> knownCommands;

    @SuppressWarnings("unchecked")
    private void cacheCommandMap() throws Exception {
        Server server = Rebar.server();
        if (commandMap == null) {
            commandMap = (SimpleCommandMap)
                    ReflectionUtil.field(SimplePluginManager.class, server.getPluginManager(), "commandMap");
        }
        if (knownCommands == null) {
            Field field = commandMap.getClass().getDeclaredField("knownCommands");
            field.setAccessible(true);
            knownCommands = (Map<String, org.bukkit.command.Command>) field.get(commandMap);
        }
    }

    @Override
    public void register(CommandGroup group) {
        try {
            for (MappingEntry entry : group) {
                registerWithBukkit(entry.getName(), entry.getExecutor());
                for (String alias : entry.getAliases()) {
                    registerWithBukkit(alias, entry.getExecutor());
                }
            }
        } catch (Throwable t) {
            log.log(Level.WARNING, "Can't register commands because the command map is not available", t);
        }
    }

    @Override
    public synchronized void removeAll() {
        try {
            cacheCommandMap();
            for (String name : registered) {
                knownCommands.remove(name);
            }
            registered.clear();
        } catch (Throwable t) {
            log.log(Level.WARNING, "Can't unregister commands because the command map is not available", t);
        }
    }

    private synchronized void registerWithBukkit(String name, CommandExecutor executor) throws Exception {
        cacheCommandMap();
        name = name.toLowerCase();
        knownCommands.remove(name);
        BukkitCommand command = new BukkitCommand(name, executor);
        commandMap.register(Rebar.getInstance().getDescription().getName(), command);
        registered.add(name);
    }

}
