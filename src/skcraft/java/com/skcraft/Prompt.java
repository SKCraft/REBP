/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft;

import org.bukkit.command.CommandSender;

public interface Prompt {

    void accept(CommandSender sender, String message) throws PromptComplete;
    
    void start(CommandSender sender);

}
