/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import org.bukkit.entity.Player;

import com.sk89q.rebar.components.sessions.Managed;

public class ChannelSession implements Managed {

    private Channel channel;
    private String owner;
    private long lastDing = 0;

    public ChannelSession(Player owner) {
        this.owner = owner.getName();
    }

    public Channel getChannel() {
        return channel;
    }

    public void join(Channel channel) {
        if (channel != null) {
            leave();
        }

        this.channel = channel;
        channel.add(owner);
    }

    public void leave() {
        if (channel == null) return;
        channel.remove(owner);
        channel = null;
    }

    @Override
    public void destroy() {
        leave();
    }

    public long getLastDing() {
        return lastDing;
    }

    public boolean canDing() {
        return System.currentTimeMillis() - lastDing > 1000 * 20;
    }

    public void rememberDing() {
        this.lastDing = System.currentTimeMillis();
    }

}
