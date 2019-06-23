/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.playblock;

import com.skcraft.cardinal.util.WorldVector3i;

public class PlayBlockData {
    private WorldVector3i location;
    private int playRadiusSq = 15 * 15;
    private int stopRadiusSq = 20 * 20;
    private Media currentMedia;

    public WorldVector3i getLocation() {
        return location;
    }

    public void setLocation(WorldVector3i location) {
        this.location = location;
    }

    public int getPlayRadiusSq() {
        return playRadiusSq;
    }

    public void setPlayRadiusSq(int playRadiusSq) {
        this.playRadiusSq = playRadiusSq;
    }

    public int getStopRadiusSq() {
        return stopRadiusSq;
    }

    public void setStopRadiusSq(int stopRadiusSq) {
        this.stopRadiusSq = stopRadiusSq;
    }

    public Media getCurrentMedia() {
        return currentMedia;
    }

    public void setCurrentMedia(Media currentMedia) {
        this.currentMedia = currentMedia;
    }
}
