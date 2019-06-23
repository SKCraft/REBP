/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.compat;

import com.nijiko.permissions.PermissionHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class PermissionsBridge {

    public PermissionHandler handler;

    public PermissionsBridge(PermissionHandler handler) {
        this.handler = handler;
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    public void installVia(JavaPlugin entryPoint) throws IllegalArgumentException,
            SecurityException, IllegalAccessException, NoSuchFieldException, ClassNotFoundException, NoSuchMethodException {
    }

}
