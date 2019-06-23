/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.channels;

import java.util.HashMap;
import java.util.Map;

public class ChannelManager {

    private Map<String, Channel> channels = new HashMap<String, Channel>();

    public synchronized Channel get(String id) {
        Channel channel = channels.get(id);
        if (channel == null) {
            channel = new Channel(this, id);
            channels.put(id, channel);
        }
        return channel;
    }

    public synchronized void remove(Channel channel) {
        channels.remove(channel.getId());
    }

    public static String normalize(String string) {
        return string.replace("#", "").toLowerCase().trim();
    }

    public int size() {
        return channels.size();
    }

}
