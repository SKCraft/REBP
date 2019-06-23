/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

public class ImageResource {
    
    private static MapFont font = new MinecraftFont();
    private Image image;
    
    public ImageResource(String path) {
        try {
            InputStream stream = MapPad.class.getResourceAsStream(path);
            if (stream != null) {
                image = ImageIO.read(stream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void draw(MapCanvas canvas, int x, int y) {
        if (image == null) {
            canvas.drawText(0, 0, font, "[X]");
            return;
        }
        canvas.drawImage(x, y, image);
    }

}
