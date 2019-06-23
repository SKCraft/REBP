/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

public interface CommandManager {

    void register(CommandGroup group);

    void removeAll();

}
