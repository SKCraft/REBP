/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.apps;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import com.skcraft.mappad.AbstractApplication;
import com.skcraft.mappad.MapPad;
import com.skcraft.mappad.util.ImageRequestManager;
import com.skcraft.mappad.util.ImageRequestManager.BusyException;
import com.skcraft.mappad.util.ImgurRequest;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.HistoryHashMap;
import com.sk89q.rebar.util.MapCanvasUtil;

public class SignCodeViewer extends AbstractApplication {

    private static ImageRequestManager manager = new ImageRequestManager();
    private static HistoryHashMap<String, Image> cache = new HistoryHashMap<String, Image>(10);

    private String currentCode = null;
    private int lastImageHash = -1;
    private Image image;

    public SignCodeViewer(MapPad mapPad, Player player) {
        super(mapPad, player);
        print("New user? Type >help");
    }

    public SignCodeViewer(MapPad mapPad, Player player, String id) {
        super(mapPad, player);
        load(id);
    }
    
    private synchronized Image getCache(String id) {
        return cache.get(id);
    }
    
    private synchronized void putCache(String id, Image image) {
        cache.put(id, image);
    }
    
    public String getCurrentCode() {
        return currentCode;
    }
    
    public void load(final String id) {
        if (currentCode != null && currentCode.equals(id)) {
            return;
        }
        
        currentCode = id;
        Image cachedImage = getCache(id);
        
        // Check cache
        if (cachedImage != null) {
            image = cachedImage;
            print("SignCode " + id + " read successfully.");
            return;
        }
        
        try {
            print("Loading SignCode: " + id);
            
            manager.request(new ImgurRequest(id) {
                public void onSuccess(Image im) {
                    image = im.getScaledInstance(128, 128 - TITLE_HEIGHT, BufferedImage.SCALE_FAST);
                    putCache(id, image);
                    print("SignCode " + id + " read successfully.");
                }
                
                public void onFail(Throwable error) {
                    if (error instanceof FileNotFoundException) {
                        print("SignCode " + id + " points to a missing image.");
                    } else {
                        print("Failed to read the SignCode image!");
                    }
                }
            });
        } catch (BusyException e) {
            print("Currently busy loading an image. Please try again in a few seconds.");
        }
    }

    public void draw(MapCanvas canvas) {
        if (image == null && lastImageHash == -1) {
            MapCanvasUtil.clear(canvas, Color.WHITE);
            MapCanvasUtil.drawText(canvas, 5, 20, "Right click signs", MapPalette.DARK_GRAY);
            MapCanvasUtil.drawText(canvas, 5, 30, "with SignCodes", MapPalette.DARK_GRAY);
            MapCanvasUtil.drawText(canvas, 5, 40, "to display them here.", MapPalette.DARK_GRAY);
            
            lastImageHash = 0;
            drawTitle(canvas, "SignCodeViewer");
        } else if (image != null && image.hashCode() != lastImageHash) {
            MapCanvasUtil.clear(canvas, Color.WHITE);
            canvas.drawImage(0, TITLE_HEIGHT, image);
            lastImageHash = image.hashCode();
            drawTitle(canvas, "SignCodeViewer");
        }
    }

    public void accept(CommandContext context) throws CommandException {
        if (context.matches("help")) {
            print("Right click any sign with something like @abcdef@ on it -- this will load that image into your MapPad. The IDs between @ are IDs of images uploaded to Imgur.com.");
        } else {
            throw new CommandException("Unknown command! Try >help");
        }
    }

    public void quit() {
    }
}
