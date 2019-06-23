/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.network;

import com.comphenix.protocol.events.PacketEvent;

public interface PacketMonitor {

    void onPacketSending(PacketEvent event);

}
