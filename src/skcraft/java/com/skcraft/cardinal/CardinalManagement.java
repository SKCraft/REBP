/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.cardinal;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.skcraft.cardinal.event.ReloadEvent;
import com.skcraft.cardinal.util.event.EventBus;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CardinalManagement extends AbstractComponent {

    private EventBus eventBus;

    @Override
    public void initialize() {
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        eventBus = cardinal.getInstance(EventBus.class);
        Rebar.getInstance().registerCommands(Commands.class, this);
    }

    @Override
    public void shutdown() {

    }

    public static class Commands {
        private final CardinalManagement component;

        public Commands(CardinalManagement component) {
            this.component = component;
        }

        @Command(aliases = { "reloadcardinal" }, desc = "Issue reload event", min = 0, max = 0)
        @CommandPermissions("skcraft.cardinal.reload")
        public void reload(final CommandContext context, final CommandSender sender) throws CommandException {
            sender.sendMessage(ChatColor.YELLOW + "Reloading Cardinal modules...");
            component.eventBus.post(new ReloadEvent());
        }
    }
}
