/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import com.sk89q.minecraft.util.commands.*;
import lombok.extern.java.Log;
import org.bukkit.command.CommandSender;

@Log
public class CapsuleRootCommand {

    private final CapsuleLoader component;

    public CapsuleRootCommand(CapsuleLoader component) {
        this.component = component;
    }

    @Command(aliases = {"capsule", "cap"}, desc = "Capsule commands")
    @CommandPermissions("skcraft.capsule")
    @NestedCommand(CapsuleCommands.class)
    public void load(CommandContext context, CommandSender sender) throws CommandException {
    }

}
