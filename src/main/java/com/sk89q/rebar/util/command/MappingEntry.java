/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util.command;

import java.util.List;

public interface MappingEntry {

    String getName();

    List<String> getAliases();

    CommandExecutor getExecutor();

}
