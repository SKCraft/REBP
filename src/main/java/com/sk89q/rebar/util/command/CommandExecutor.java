/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.skcraft.rebar.Actor;

public interface CommandExecutor {

    String getDescription(Actor actor);

    boolean mayExecutePossibly(Actor actor);

    void execute(Actor actor, String[] args) throws CommandException;

}
