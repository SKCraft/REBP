/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

import org.bukkit.map.MapCanvas;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

public interface Application {
    
    public void draw(MapCanvas canvas);

    public void accept(CommandContext context) throws CommandException;
    
    public void quit();
    
}
