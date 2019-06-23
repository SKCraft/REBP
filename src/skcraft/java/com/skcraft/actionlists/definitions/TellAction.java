/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import com.skcraft.actionlists.Action;
import com.skcraft.actionlists.BukkitContext;
import com.skcraft.actionlists.ExpressionParser;

public class TellAction implements Action<BukkitContext> {

    private ExpressionParser parser;
    private String message;

    public TellAction(String message) {
        this.message = message;
    }

    public ExpressionParser getParser() {
        return parser;
    }

    public void setParser(ExpressionParser parser) {
        this.parser = parser;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void apply(BukkitContext context) {
        Entity sender = context.getSource();

        if (sender != null && sender instanceof CommandSender) {
            if (parser != null) {
                message = parser.format(context, message);
            }
            ((CommandSender) sender).sendMessage(message);
        }
    }

}
