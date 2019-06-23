/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Component;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.Unlisted;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.rebar.util.StringUtil;

public class BasicCommands extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    private static String compactPath(String path) {
        String[] parts = path.split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            parts[i] = parts[i].substring(0, 1);
        }
        parts[0] = ChatColor.DARK_AQUA + parts[0];
        parts[parts.length - 1] = ChatColor.AQUA + parts[parts.length - 1];
        return StringUtil.joinString(parts, ".");
    }

    public static class Commands {
        public Commands(BasicCommands component) {
        }

        @Command(aliases = { "version" }, desc = "Version information")
        public void version(CommandContext context, CommandSender sender) {
            ChatUtil.msg(sender, ChatColor.YELLOW, "SKCraft/Rebar version 3.0");
        }

        @Command(aliases = { "plugins", "pl", "components" }, desc = "Components information")
        public void components(CommandContext context, CommandSender sender) {
            StringBuilder message = new StringBuilder(ChatColor.YELLOW + "Loaded components: ");
            boolean first = true;
            for (Component component : Rebar.getInstance().getLoader().getLoaded()) {
                if (component.getClass().isAnnotationPresent(Unlisted.class)) {
                    continue;
                }
                if (!first) {
                    message.append(ChatColor.YELLOW + ", ");
                }
                message.append(compactPath(component.getClass().getCanonicalName()));
                first = false;
            }
            sender.sendMessage(message.toString());
        }

        @Command(aliases = { "reload" }, desc = "Disabled command")
        public void reload(CommandContext context, CommandSender sender) throws CommandException {
            throw new CommandException("Can't use this, sorry.");
        }

    }

}
