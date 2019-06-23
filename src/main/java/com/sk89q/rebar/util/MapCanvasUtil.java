/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MapFont.CharacterSprite;
import org.bukkit.map.MinecraftFont;

public class MapCanvasUtil {
    
    public static final MapFont MC_FONT = new MinecraftFont();

    public static void drawText(MapCanvas canvas, int x, int y, String text, byte color) {
        drawText(canvas, x, y, MC_FONT, text, color);
    }

    public static void drawText(MapCanvas canvas, int x, int y, MapFont font, String text, byte color) {
        int xStart = x;
        if (!font.isValid(text)) {
            throw new IllegalArgumentException("Text contains invalid characters");
        }
        
        for (int i = 0; i < text.length(); ++i) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                x = xStart;
                y += font.getHeight() + 1;
                continue;
            }
            
            CharacterSprite sprite = font.getChar(text.charAt(i));
            for (int r = 0; r < font.getHeight(); ++r) {
                for (int c = 0; c < sprite.getWidth(); ++c) {
                    if (sprite.get(r, c)) {
                        canvas.setPixel(x + c, y + r, color);
                    }
                }
            }
            x += sprite.getWidth() + 1;
        }
    }

    public static void clear(MapCanvas canvas, int x0, int y0, int width, int height, byte color) {
        for (int x = x0; x < x0 + width; x++) {
            for (int y = y0; y < y0 + height; y++) {
                canvas.setPixel(x, y, color);
            }
        }
    }

    public static void clear(MapCanvas canvas, byte color) {
        clear(canvas, 0, 0, 128, 128, color);
    }

    public static void clear(MapCanvas canvas, Color color) {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(color);
        graphics.fill(new Rectangle(0, 0, 128, 128));
        graphics.dispose();
        canvas.drawImage(0, 0, image);
    }
    
}
