/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;

import com.sk89q.rebar.util.MapCanvasUtil;

public abstract class AbstractApplication implements Application {

    public static final int TITLE_HEIGHT = 11;

    private static ImageResource titleBackground = new ImageResource("/resources/mappad_bar.png");

    private MapPad mapPad;
    private Player player;
    private MapPadSession session;

    public AbstractApplication(MapPad mapPad, Player player) {
        this.setMapPad(mapPad);
        this.setPlayer(player);
    }

    public void drawTitle(MapCanvas canvas, String title) {
        titleBackground.draw(canvas, 0, 0);
        MapCanvasUtil.drawText(canvas, 1, 1, title, MapPalette.DARK_GRAY);
    }

    public MapPad getMapPad() {
        return mapPad;
    }

    public void setMapPad(MapPad mapPad) {
        this.mapPad = mapPad;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public MapPadSession getSession() {
        if (session == null) {
            this.setSession(mapPad.getSession(player));
        }
        return session;
    }

    public void setSession(MapPadSession session) {
        this.session = session;
    }

    public void print(String message) {
        MapPad.print(getPlayer(), message);
    }

    public void printError(String message) {
        MapPad.print(getPlayer(), message);
    }

}
