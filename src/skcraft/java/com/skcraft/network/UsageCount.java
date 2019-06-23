/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.network;

/**
 * Usage count structure, required because accessing the map twice to get the current
 * values and the new value would be less efficient.
 */
public class UsageCount implements Comparable<UsageCount> {

    private final String id;
    private int totalBytes;
    private int numPackets;

    UsageCount(UsageCount count) {
        this.id = count.getId();
        this.totalBytes = count.totalBytes;
        this.numPackets = count.numPackets;
    }

    UsageCount(String id) {
        this.id = id;
    }

    /**
     * Log a send.
     *
     * @param bytes number of bytes.
     */
    void add(int bytes) {
        this.totalBytes += bytes;
        this.numPackets++;
    }

    /**
     * Get the ID for this count.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the total number of bytes sent.
     *
     * @return number of bytes
     */
    public int getTotalBytes() {
        return totalBytes;
    }

    /**
     * Get the average number of bytes per packet.
     *
     * @return number of bytes
     */
    public double getAveragePerPacket() {
        return totalBytes / numPackets;
    }

    /**
     * Get the number of packets.
     *
     * @return number of packets
     */
    public int getPacketCount() {
        return numPackets;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UsageCount)) {
            return false;
        }

        return ((UsageCount) obj).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public int compareTo(UsageCount o) {
        if (o.totalBytes < totalBytes) {
            return 1;
        } else if (o.totalBytes > totalBytes) {
            return -1;
        } else {
            return 0;
        }
    }
}
