/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import lombok.extern.java.Log;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;

@Log
public class CapsuleCommands {

    private final CapsuleLoader component;

    public CapsuleCommands(CapsuleLoader component) {
        this.component = component;
    }

    @Command(aliases = {"load"}, desc = "Load a capsule", usage = "<script>", min = 1, max = 1)
    @CommandPermissions("skcraft.capsule")
    public void load(CommandContext context, CommandSender sender) throws CommandException {
        if (sender instanceof ConsoleCommandSender) {
            try {
                String name = context.getString(0);
                component.load(name);
                component.setLoadOnBoot(name, true);
                sender.sendMessage(ChatColor.YELLOW + "The script '" + name + "' was loaded.");
            } catch (FileNotFoundException e) {
                CapsuleCommands.log.log(Level.WARNING, "Script file does not exist", e);
            } catch (IOException e) {
                CapsuleCommands.log.log(Level.WARNING, "Failed to load script", e);
                throw new CommandException("Failed to load the given script: " + e.getMessage());
            }
        } else {
            throw new CommandException("Sorry, you can't do that.");
        }
    }

    @Command(aliases = {"unload"}, desc = "Unload a capsule", usage = "<script>", min = 1, max = 1)
    @CommandPermissions("skcraft.capsule")
    public void unload(CommandContext context, CommandSender sender) throws CommandException {
        if (sender instanceof ConsoleCommandSender) {
            String name = context.getString(0);
            boolean existed = component.unload(name);
            component.setLoadOnBoot(name, false);
            if (existed) {
                sender.sendMessage(ChatColor.YELLOW + "The script '" + name + "' was unloaded.");
            } else {
                throw new CommandException("The script '" + name + "' was not loaded.");
            }
        } else {
            throw new CommandException("Sorry, you can't do that.");
        }
    }

    @Command(aliases = {"reload"}, desc = "Reload a capsule", usage = "<script>", min = 1, max = 1)
    @CommandPermissions("skcraft.capsule")
    public void reload(CommandContext context, CommandSender sender) throws CommandException {
        if (sender instanceof ConsoleCommandSender) {
            unload(context, sender);
            load(context, sender);
        } else {
            throw new CommandException("Sorry, you can't do that.");
        }
    }

}
