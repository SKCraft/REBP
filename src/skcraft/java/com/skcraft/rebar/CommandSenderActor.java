/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.rebar;

import lombok.Getter;
import org.bukkit.command.CommandSender;

public class CommandSenderActor implements Actor {

    @Getter
    private final CommandSender sender;

    public CommandSenderActor(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public Object getHandle() {
        return sender;
    }

    @Override
    public void message(String message) {
        sender.sendMessage(message);
    }

}
