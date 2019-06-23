/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class ImmutableMappingEntry implements MappingEntry {

    @Getter
    private final String name;
    @Getter
    private final List<String> aliases;
    @Getter
    private final CommandExecutor executor;

    public ImmutableMappingEntry(String name, List<String> aliases, CommandExecutor executor) {
        this.name = name;
        this.aliases = Collections.unmodifiableList(aliases);
        this.executor = executor;
    }
}
