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

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActionBarMessages extends AbstractComponent implements Runnable {
    private static final Logger log = Logger.getLogger(ActionBarMessages.class.getName());
    private ScheduledExecutorService timer;
    private NoticeManager noticeManager;
    private ProtocolManager protocolManager;

    @Override
    public void initialize() {
        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        noticeManager = cardinal.getInstance(NoticeManager.class);
        Rebar.getInstance().registerEvents(this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleWithFixedDelay(this, 5, 5, TimeUnit.MINUTES);
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void run() {
        Futures.addCallback(noticeManager.getNext("action_bar"), new FutureCallback<Notice>() {
            @Override
            public void onSuccess(@Nullable Notice result) {
                log.log(Level.INFO, "Got Action Bar message: " + (result != null ? result.getMessage() : "NULL"));

                if (result == null) return;

                Runnable messageRunnable = () -> {
                    WrappedChatComponent message = WrappedChatComponent.fromText(ChatUtil.replaceColorMacros(result.getMessage()));
                    broadcastMessage(message);
                };

                Runnable wrapperRunnable = () -> Rebar.getInstance().registerTimeout(messageRunnable, 0);

                for (int i = 0; i <= 5000; i += 500) {
                    timer.schedule(wrapperRunnable, i, TimeUnit.MILLISECONDS);
                }
            }

            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    public void broadcastMessage(WrappedChatComponent message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PacketContainer packetContainer = protocolManager.createPacket(PacketType.Play.Server.CHAT);
            packetContainer.getChatComponents().write(0, message);
            packetContainer.getBytes().write(0, (byte) 2);

            try {
                protocolManager.sendServerPacket(player, packetContainer);
            } catch (InvocationTargetException e) {
                log.log(Level.WARNING, "Cannot send packet " + packetContainer, e);
            }
        }
    }
}
