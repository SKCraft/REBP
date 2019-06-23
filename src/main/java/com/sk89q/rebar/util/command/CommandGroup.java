/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import java.util.List;

public interface CommandGroup extends CommandExecutor, Iterable<MappingEntry> {

    void register(String name, CommandExecutor executor);

    void register(String name, CommandExecutor executor, List<String> aliases);

}
