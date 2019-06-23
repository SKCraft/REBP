/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.rebar.CommandSenderActor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class BukkitCommand extends Command {

    private final CommandExecutor executor;

    public BukkitCommand(String name, CommandExecutor executor) {
        super(name);
        this.executor = executor;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        try {
            String[] argsWithCommand = new String[args.length + 1];
            argsWithCommand[0] = commandLabel;
            System.arraycopy(args, 0, argsWithCommand, 1, args.length);
            executor.execute(new CommandSenderActor(sender), argsWithCommand);
        } catch (CommandException e) {
            ChatUtil.msg(sender, ChatColor.RED, e.getMessage());
        }
        return true;
    }

}
