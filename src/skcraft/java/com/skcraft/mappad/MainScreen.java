/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.skcraft.mappad;

import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.MapCanvasUtil;

public class MainScreen extends AbstractApplication {

    private static Image img;
    private static MapFont font = new MinecraftFont();

    private boolean needsRender = true;
    
    static {
        try {
        	InputStream stream = MapPad.class.getResourceAsStream("/resources/mappad_default_bg.png");
            if (stream != null) {
                img = ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public MainScreen(MapPad mapPad, Player player) {
        super(mapPad, player);
    }
    
    public void draw(MapCanvas canvas) {
        if (!needsRender) return;
        needsRender = false;
        
        if (img != null) {
            canvas.drawImage(0, 0, img);
            drawTitle(canvas, "MapPad 2");
        } else {
            MapCanvasUtil.clear(canvas, Color.WHITE);
            canvas.drawText(0, 20, font, "Failed to render");
            drawTitle(canvas, "MapPad 2");
        }

        print("Welcome to the MapPad! Type >apps to get started.");
    }

    public void accept(CommandContext context) throws CommandException {
        throw new CommandException("Type >apps to get started.");
    }

    public void quit() {
    }
    
}
