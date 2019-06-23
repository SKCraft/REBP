/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import org.bukkit.entity.Player;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

public interface WandFactory {
    
    public WandActor create(Player player, CommandContext context) throws CommandException;
    
    public boolean hasPermission(Player player);
    
    public String getName();

}
