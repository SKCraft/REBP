/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.concurrent.ExecutionException;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.minecraft.util.commands.CommandUsageException;
import com.sk89q.minecraft.util.commands.MissingNestedCommandException;
import com.sk89q.minecraft.util.commands.UnhandledCommandException;
import com.sk89q.minecraft.util.commands.WrappedCommandException;

public abstract class CommandRunnable implements Runnable {

    private CommandContext context;
    private CommandSender sender;

    public CommandRunnable(CommandContext context, CommandSender sender) {
        this.context = context;
        this.sender = sender;
    }

    public abstract void execute(CommandContext context, CommandSender sender)
            throws CommandException, InterruptedException, ExecutionException;

    @Override
    public void run() {
        try {
            execute(context, sender);
        } catch (NumberFormatException e) {
            ChatUtil.error(sender, "The command expected you to enter a number but instead you entered words.");
        } catch (CommandPermissionsException e) {
            ChatUtil.error(sender, "You do not have the sufficient permission to do this.");
        } catch (MissingNestedCommandException e) {
            ChatUtil.error(sender, e.getUsage());
        } catch (CommandUsageException e) {
            ChatUtil.error(sender, e.getMessage());
            ChatUtil.error(sender, e.getUsage());
        } catch (WrappedCommandException e) {
            ChatUtil.error(sender, "An error occurred while processing the command: " + e.getMessage());
            e.printStackTrace();
        } catch (UnhandledCommandException e) {
            return;
        } catch (CommandException e) {
            ChatUtil.error(sender, e.getMessage());
        } catch (Throwable e) {
            if (e.getMessage() != null)
                ChatUtil.error(sender, e.getMessage());
            e.printStackTrace();
        }
    }

}