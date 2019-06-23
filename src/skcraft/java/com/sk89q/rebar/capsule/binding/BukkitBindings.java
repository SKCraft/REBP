/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule.binding;

import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.command.BukkitCommandManager;
import com.sk89q.rebar.util.command.CommandGroup;
import com.sk89q.rebar.util.command.CommandManager;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public final class BukkitBindings {

    private BukkitBindings() {
    }

    public static void bindListeners(@NonNull BindingGuard guard, final @NonNull Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, Rebar.getInstance());
        guard.add(new Runnable() {
            @Override
            public void run() {
                HandlerList.unregisterAll(listener);
            }
        });
    }

    public static void bindCommands(@NonNull BindingGuard guard, @NonNull CommandGroup group) {
        final CommandManager manager = new BukkitCommandManager();
        manager.register(group);
        guard.add(new Runnable() {
            @Override
            public void run() {
                manager.removeAll();
            }
        });
    }

}
