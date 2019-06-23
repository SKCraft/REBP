/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import bsh.EvalError;
import bsh.Interpreter;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BeanShell extends AbstractComponent {

    private static Logger logger = createLogger(BeanShell.class);

    @Override
    public void initialize() {
        Rebar.getInstance().registerCommands(BeanShellCommands.class, this);
    }

    @Override
    public void shutdown() {
    }

    public static class BeanShellCommands {
        private final BeanShell component;

        public BeanShellCommands(BeanShell component) {
            this.component = component;
        }

        public Interpreter createInterpreter() {
            Interpreter interpreter = new Interpreter();
            try {
                interpreter.set("out", System.out);
                interpreter.set("rebar", Rebar.getInstance());
                interpreter.set("loader", Rebar.getInstance().getLoader());
                interpreter.set("server", Rebar.getInstance().getServer());
                interpreter.set("component", this);
            } catch (EvalError e) {
                logger.log(Level.WARNING, "Error evaluating", e);
            }

            return interpreter;
        }

        @Command(aliases = { "bsh" }, desc = "Execute beanshell", min = 1, max = -1)
        @CommandPermissions("skcraft.beanshell")
        public void bsh(CommandContext context, CommandSender sender) throws CommandException {
            if (!(sender instanceof ConsoleCommandSender)) {
                throw new CommandException("This must be run from console.");
            }

            try {
                Interpreter interpreter = createInterpreter();
                interpreter.set("args", context);
                Object obj = interpreter.eval(context.getJoinedStrings(0));

                if (obj != null)
                    sender.sendMessage(">>> " + String.valueOf(obj));
            } catch (EvalError e) {
                sender.sendMessage("!!! " + e.getMessage());
            }
        }

        @Command(aliases = { "bshfile" }, desc = "Execute beanshell script", min = 1, max = -1)
        @CommandPermissions("skcraft.beanshell")
        public void bshFile(CommandContext context, CommandSender sender) throws CommandException {
            if (!(sender instanceof ConsoleCommandSender)) {
                throw new CommandException("This must be run from console.");
            }

            try {
                Interpreter interpreter = createInterpreter();
                interpreter.set("args", context);
                Object obj = interpreter.source(context.getJoinedStrings(0));

                if (obj != null)
                    sender.sendMessage(">>> " + String.valueOf(obj));
            } catch (EvalError | IOException e) {
                sender.sendMessage("!!! " + e.getMessage());
            }
        }
    }


}
