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
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.sk89q.worldedit.expression.Expression;
import com.sk89q.worldedit.expression.ExpressionException;

public class Calculator extends AbstractComponent {

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class Commands {
        public Commands(Calculator component) {
        }

        @Command(aliases = {"eval", "calc", "calculate", "evaluate", "math", "c"}, desc = "Evaluate a mathematical expression",
                 usage = "<expr>", min = 1, max = -1)
        public void eval(CommandContext context, CommandSender sender) throws CommandException {
            try {
                String str = context.getJoinedStrings(0).trim();
                Expression expr = Expression.compile(str);
                ChatUtil.msg(sender, ChatColor.YELLOW, str, ChatColor.WHITE, " = ", ChatColor.AQUA, expr.evaluate());
            } catch (ExpressionException e) {
                throw new CommandException("Could not parse expression: " + e.getMessage());
            }
        }

    }

}
