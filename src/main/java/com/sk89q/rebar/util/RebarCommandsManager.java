/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import org.bukkit.command.CommandSender;

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.rebar.Rebar;

public class RebarCommandsManager extends CommandsManager<CommandSender> {

    @Override
    public boolean hasPermission(CommandSender player, String perm) {
        return Rebar.getInstance().hasPermission(player, perm);
    }

}
