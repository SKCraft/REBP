/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import bsh.EvalError;
import bsh.Interpreter;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;

public class BeanShellWandFactory implements WandFactory {

    private static Logger logger = Logger.getLogger(BeanShellWandFactory.class
            .getCanonicalName());
    private final File path;
    private final String description;
    private final String permission;

    public BeanShellWandFactory(File path) {
        this.path = path;
        this.description = null;
        this.permission = "skcraft.magic-wand.scripted.script."
                + path.getName().replace(".", "_");
    }

    public BeanShellWandFactory(File path, String description, String permission) {
        this.path = path;
        this.description = description;
        if (permission != null) {
            this.permission = permission;
        } else {
            this.permission = "skcraft.magic-wand.scripted.script."
                    + path.getName().replace(".", "_");
        }
    }

    @Override
    public WandActor create(Player player, CommandContext context)
            throws CommandException {
        try {
            return new BeanShellWand();
        } catch (FileNotFoundException e) {
            throw new CommandException("Could not find file " + path.getName());
        } catch (IOException e) {
            throw new CommandException("IOException");
        } catch (EvalError e) {
            logger.log(Level.WARNING, "BeanShellWand: Failed to load wand "
                    + path.getName(), e);
            throw new CommandException("Wand failed to properly load.");
        }
    }

    @Override
    public String getName() {
        return description != null ? description : "Scripted: " + path.getName();
    }

    @Override
    public boolean hasPermission(Player player) {
        return Rebar.getInstance().hasPermission(player, permission);
    }

    private class BeanShellWand implements WandActor {

        private Interpreter interpreter;

        public BeanShellWand() throws FileNotFoundException, IOException, EvalError {
            interpreter = new Interpreter();
            try {
                interpreter.set("out", System.out);
                interpreter.set("rebar", Rebar.getInstance());
                interpreter.set("loader", Rebar.getInstance().getLoader());
                interpreter.set("server", Rebar.getInstance().getServer());
            } catch (EvalError e) {
                logger.log(Level.WARNING, "BeanShellWand: Failed to set vars", e);
            }
            interpreter.source(path.getAbsolutePath());
        }

        @Override
        public String getName() {
            return description != null ? description : "Scripted: " + path.getName();
        }

        private Object executeMethod(String methodName, Object ... args) throws EvalError {
            try {
                StringBuilder callBuilder = new StringBuilder();
                callBuilder.append(methodName);
                callBuilder.append("(");
                for (int i = 0; i < args.length; i++) {
                    interpreter.set("__arg" + i, args[i]);
                    if (i != 0) {
                        callBuilder.append(",");
                    }
                    callBuilder.append("__arg");
                    callBuilder.append(i);
                }
                callBuilder.append(")");

                return interpreter.eval(callBuilder.toString());
            } catch (EvalError e) {
                logger.log(Level.WARNING, "BeanShellWand: Failed to evaluate " + methodName + "():\r\n" +
                        "\tError: " + e.getMessage() + "\r\n" +
                        "\tLine: " + e.getErrorText() + "\r\n" +
                        "\tLocation: " + e.getErrorSourceFile() + ":" + e.getErrorLineNumber());
                throw e;
            } finally {
                for (int i = 0; i < args.length; i++) {
                    interpreter.set("__arg" + i, null);
                }
            }
        }

        @Override
        public String getHelp() {
            try {
                return String.valueOf(executeMethod("getHelp"));
            } catch (EvalError e) {
                return "<EVALUATION ERROR>";
            }
        }

        @Override
        public boolean interact(Player player, Action action, Block block,
                PlayerInteractEvent event) {
            try {
                Object ret = executeMethod("interact", player, action, block, event);
                if (ret instanceof Boolean) {
                    return (Boolean) ret;
                }
                return true;
            } catch (EvalError e) {
                ChatUtil.error(player, "<EVALUATION ERROR: " + ChatColor.DARK_GRAY + "[@"
                        + e.getErrorLineNumber() + "] "
                        + ChatColor.GRAY
                        + e.getMessage() + ChatColor.RED + ">");
                return true;
            }
        }

        @Override
        public boolean hasPermissionStill(Player player) {
            return hasPermission(player);
        }

        @Override
        public void destroy() {
            try {
                executeMethod("destroy");
            } catch (EvalError e) {
            }
        }
    }

}
