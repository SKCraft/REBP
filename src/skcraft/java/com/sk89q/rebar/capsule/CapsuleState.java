/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.RebarCommandsManager;
import com.sk89q.rebar.util.ReflectionUtil;

public class CapsuleState {

    private static final Logger logger = Logger.getLogger(CapsuleState.class.getCanonicalName());
    private static final Map<String, Command> commandBackup = new HashMap<String, Command>();

    private final CommandsManager<CommandSender> commands;

    public CapsuleState() {
        commands = new RebarCommandsManager();
    }

    public void registerCommands(Class<?> cls) {
        applyCommands(); // Make sure that we can use the commands!
    }

    private synchronized void applyCommands() {
        Server server = Rebar.server();

        try {
            SimpleCommandMap commandMap = (SimpleCommandMap) ReflectionUtil.field(
                    SimplePluginManager.class, server.getPluginManager(), "commandMap");
            Constructor<PluginCommand> constr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constr.setAccessible(true);

            for (Map.Entry<String, String> entry : commands.getCommands().entrySet()) {
                // Store the old command in case we need to restore it
                Command previousCommand = commandMap.getCommand(entry.getKey());
                if (previousCommand != null && !commandBackup.containsKey(entry.getKey())) {
                    commandBackup.put(entry.getKey(), previousCommand);
                } else {
                    commandBackup.put(entry.getKey(), null); // Store the fact
                                                             // that this is one
                                                             // of our commands
                }

                Command replacementCommand = constr.newInstance(entry.getKey(), this);
                replacementCommand.setDescription(entry.getValue());
                commandMap.register(Rebar.getInstance().getDescription().getName(), replacementCommand);
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Rebar: Couldn't register command!", e);
        }
    }

}
