/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.adventure;

import org.bukkit.World;

public interface WorldFactory {

    World create(String name);

    void init(World world);

    String getId();

}
