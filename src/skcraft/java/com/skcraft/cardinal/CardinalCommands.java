/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.cardinal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.skcraft.cardinal.profile.ConsoleUser;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.profile.ProfileId;
import com.skcraft.cardinal.service.hive.permission.Context;
import com.skcraft.cardinal.service.remotecommand.CommandException;
import com.skcraft.cardinal.service.remotecommand.RemoteCommandManager;
import com.skcraft.cardinal.service.remotecommand.Response;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CardinalCommands extends AbstractComponent implements Listener {

    private static final Logger logger = Logger.getLogger(CardinalUsers.class.getCanonicalName());

    private RemoteCommandManager commandManager;

    @Override
    public void initialize() {
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        commandManager = cardinal.getInstance(RemoteCommandManager.class);
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().replaceAll("^/", "");
        MojangId mojangId = new MojangId(event.getPlayer().getUniqueId(), event.getPlayer().getName());
        if (executeCommand(event.getPlayer(), mojangId, command)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onServerCommand(final ServerCommandEvent event) {
        String command = event.getCommand().replaceAll("^/", "");
        if (executeCommand(event.getSender(), new ConsoleUser(), command)) {
            event.setCancelled(true);
        }
    }

    private boolean executeCommand(final CommandSender sender, ProfileId profileId, String command) {
        Context context = new Context.Builder().build();
        ListenableFuture<Response> future = commandManager.execute(profileId, command, context);
        if (future != null) {
            Futures.addCallback(future, new FutureCallback<Response>() {
                @Override
                public void onSuccess(Response result) {
                    sender.sendMessage(ChatColor.YELLOW + result.getMessage());
                }

                @Override
                public void onFailure(Throwable t) {
                    if (t instanceof CommandException) {
                        sender.sendMessage(ChatColor.RED + t.getMessage());
                    } else {
                        sender.sendMessage(ChatColor.RED + "An error has occurred and your command could not be processed.");
                        logger.log(Level.WARNING, "Failed to execute command", t);
                    }
                }
            });

            return true;
        } else {
            return false;
        }
    }

}
