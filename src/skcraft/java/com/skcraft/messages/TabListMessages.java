/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.messages;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.util.ChatUtil;
import com.skcraft.cardinal.Cardinal;
import com.skcraft.cardinal.service.notice.Notice;
import com.skcraft.cardinal.service.notice.NoticeManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TabListMessages extends AbstractComponent implements Runnable, Listener {
    private static final Logger log = Logger.getLogger(ActionBarMessages.class.getName());
    private final ScheduledExecutorService timer = Executors.newScheduledThreadPool(1);
    private NoticeManager noticeManager;
    private ProtocolManager protocolManager;
    private MessageBundle messages;

    @Override
    public void initialize() {
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        noticeManager = cardinal.getInstance(NoticeManager.class);
        Rebar.getInstance().registerEvents(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        timer.scheduleWithFixedDelay(this, 0, 1, TimeUnit.MINUTES);
        Rebar.getInstance().registerEvents(this);
    }

    @Override
    public void shutdown() {
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        sendTabListMessages(event.getPlayer(), messages);
    }

    public void sendTabListMessages(Player player, MessageBundle messages) {
        if (messages != null) {
            PacketContainer packetContainer = protocolManager.
                    createPacket(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);

            packetContainer.getChatComponents().
                    write(0, messages.header).
                    write(1, messages.footer);

            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                log.log(Level.WARNING, "Cannot send packet " + packetContainer, e);
            }
        }
    }

    @Override
    public void run() {
        Futures.addCallback(noticeManager.getNext("tab_list"), new FutureCallback<Notice>() {
            @Override
            public void onSuccess(@Nullable Notice result) {
                if (result != null) {
                    StringBuilder header = new StringBuilder();
                    StringBuilder footer = new StringBuilder();
                    boolean firstLine = true;
                    boolean inHeader = true;
                    for (String line : result.getMessage().split("\\r?\\n")) {
                        if (line.equalsIgnoreCase("---SPLIT---")) {
                            inHeader = false;
                            firstLine = true;
                        } else if (inHeader) {
                            if (firstLine) {
                                firstLine = false;
                            } else {
                                header.append("\n");
                            }
                            header.append(line);
                        } else {
                            if (firstLine) {
                                firstLine = false;
                            } else {
                                footer.append("\n");
                            }
                            footer.append(line);
                        }
                    }

                    messages = new MessageBundle(
                            WrappedChatComponent.fromText(ChatUtil.replaceColorMacros(header.toString())),
                            WrappedChatComponent.fromText(ChatUtil.replaceColorMacros(footer.toString())));

                    Rebar.getInstance().registerTimeout(() -> {
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            sendTabListMessages(player, messages);
                        }
                    }, 1);
                } else {
                    messages = null;
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    private static class MessageBundle {
        private final WrappedChatComponent header;
        private final WrappedChatComponent footer;

        private MessageBundle(WrappedChatComponent header, WrappedChatComponent footer) {
            this.header = header;
            this.footer = footer;
        }
    }
}
