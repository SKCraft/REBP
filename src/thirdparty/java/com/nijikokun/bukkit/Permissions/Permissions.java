package com.nijikokun.bukkit.Permissions;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
//import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import com.nijiko.permissions.PermissionHandler;

/**
 * Permissions 3.x Copyright (C) 2011 Matt 'The Yeti' Burnett
 * <admin@theyeticave.net> Original Credit & Copyright (C) 2010 Nijikokun
 * <nijikokun@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Permissions Public License as published by the Free
 * Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Permissions Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Permissions Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

public class Permissions extends JavaPlugin {

    public static Plugin instance;
    @Deprecated
    public static final String name = "Permissions";
    public static final String version = "3.2";
    public static final String codename = "Yeti";

    /**
     * Controller for permissions and security. Use getHandler() instead.
     */
    public static PermissionHandler Security;

    public Permissions(PermissionHandler handler) {
        Permissions.instance = this;
        Permissions.Security = handler;
    }
    
    public void doInitialize(PluginLoader loader, Server server,
            PluginDescriptionFile description, ClassLoader classLoader) {
        initialize(loader, server, description, null, null, classLoader);
    }

    public void onDisable() {
    }

    public void onEnable() {
    }

    /**
     * Returns the PermissionHandler instance.<br />
     * <br />
     * <blockquote>
     * 
     * <pre>
     * Permissions.getHandler()
     * </pre>
     * 
     * </blockquote>
     * 
     * @return PermissionHandler
     */
    public PermissionHandler getHandler() {
        return Permissions.Security;
    }
}