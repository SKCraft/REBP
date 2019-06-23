/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;

public class CoreCommands extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class Commands {
        public Commands(CoreCommands component) {
        }

        @Command(aliases = { "reload-rebar" }, desc = "Reload rebar")
        @CommandPermissions({ "rebar.reload" })
        public void reloadRebar(CommandContext context, CommandSender sender) {
            Rebar.getInstance().reload();
            ChatUtil.msg(sender, ChatColor.YELLOW, "Reloaded.");
        }
    }

}
