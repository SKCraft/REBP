/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.services;

import org.bukkit.generator.ChunkGenerator;

public interface DefaultWorldProvider {
    ChunkGenerator getDefaultWorldGenerator(String worldName, String id);
}
