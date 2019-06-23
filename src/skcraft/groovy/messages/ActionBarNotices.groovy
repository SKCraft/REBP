/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package messages

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.sk89q.rebar.Rebar
import com.sk89q.rebar.capsule.AbstractCapsule
import com.sk89q.rebar.capsule.binding.BindingGuard
import com.sk89q.rebar.capsule.binding.BukkitBindings
import com.sk89q.rebar.util.ChatUtil
import com.skcraft.cardinal.Cardinal
import com.skcraft.cardinal.service.notice.Notice
import com.skcraft.cardinal.service.notice.NoticeManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener

import javax.annotation.Nullable
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class ActionBarNotices extends AbstractCapsule implements Listener, Runnable {

    private static final Logger log = Logger.getLogger(ActionBarNotices.class.getName());
    private ScheduledExecutorService timer;
    private NoticeManager noticeManager;
    private ProtocolManager protocolManager;

    @Override
    void preBind() {
        System.out.println("SKCraft Action Bar Messages r3 loading...");

        Cardinal cardinal = Cardinal.load(); // Might throw an exception
        noticeManager = cardinal.getInstance(NoticeManager.class);
        protocolManager = ProtocolLibrary.getProtocolManager();
        timer = Executors.newScheduledThreadPool(1);
        timer.scheduleWithFixedDelay(this, 0, 5, TimeUnit.MINUTES);

        BindingGuard guard = getGuard();
        BukkitBindings.bindListeners(guard, this);
        guard.add(new Runnable() {
            @Override
            public void run() {
                timer.shutdownNow();
            }
        });
    }

    @Override
    public void run() {
        Futures.addCallback(noticeManager.getNext("action_bar"), new FutureCallback<Notice>() {
            @Override
            public void onSuccess(@Nullable Notice result) {
                log.log(Level.INFO, "Got Action Bar message: " + (result != null ? result.getMessage() : "NULL"));

                if (result == null) return;

                Runnable messageRunnable = new Runnable() {
                    @Override
                    void run() {
                        WrappedChatComponent message = WrappedChatComponent.fromText(ChatUtil.replaceColorMacros(result.getMessage()));
                        broadcastMessage(message);
                    }
                };

                Runnable wrapperRunnable = new Runnable() {
                    @Override
                    void run() {
                        Rebar.getInstance().registerTimeout(messageRunnable, 0);
                    }
                };

                for (int i = 0; i <= 3000; i += 500) {
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
