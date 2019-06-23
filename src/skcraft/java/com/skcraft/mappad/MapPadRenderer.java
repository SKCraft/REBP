/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class MapPadRenderer extends MapRenderer {
    
    private MapPad mapPad;
    private int renderIndex = 0;
    
    public MapPadRenderer(MapPad mapPad) {
        super(true);
        this.mapPad = mapPad;
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        renderIndex++;
        if (renderIndex % 10 != 0) return;
        renderIndex = 0;
        
        mapPad.getSession(player).getApplication().draw(canvas);
    }
    
}
