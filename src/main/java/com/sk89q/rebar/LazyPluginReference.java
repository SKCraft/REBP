/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar;

import org.bukkit.plugin.Plugin;

public class LazyPluginReference<T extends Plugin> {
    
    private Class<T> seekingClass;
    private T plugin;
    
    public LazyPluginReference(Class<T> seekingClass) {
        this.seekingClass = seekingClass;
    }
    
    @SuppressWarnings("unchecked")
    public T get() {
        if (plugin == null) {
            for (Plugin plugin : Rebar.getInstance().getServer().getPluginManager().getPlugins()) {
                if (plugin.getClass() == seekingClass) {
                    this.plugin = (T) plugin;
                    break;
                }
            }
        }
        
        return plugin;
    }

}
