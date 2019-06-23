/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.apps;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapFont;
import org.bukkit.map.MinecraftFont;

import com.skcraft.mappad.AbstractApplication;
import com.skcraft.mappad.MapPad;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.rebar.util.MapCanvasUtil;

public class ImgurBrowser extends AbstractApplication {

    private static final Logger logger = Logger.getLogger(ImgurBrowser.class.getCanonicalName());
    private static ImgurRequestManager manager = new ImgurRequestManager();
    private static MapFont font = new MinecraftFont();
    
    private int lastImageHash = -1;

    public ImgurBrowser(MapPad mapPad, Player player) {
        super(mapPad, player);
        print("New users: type >help");
    }

    public void draw(MapCanvas canvas) {
        Image image = manager.getImage();
        
        if (image == null && lastImageHash == -1) {
            MapCanvasUtil.clear(canvas, Color.WHITE);
            canvas.drawText(0, 20, font, "No image loaded.");
            canvas.drawText(0, 30, font, "Use >load # to post");
            drawTitle(canvas, "Imgur");
            lastImageHash = 0;
        } else if (image != null && image.hashCode() != lastImageHash) {
            MapCanvasUtil.clear(canvas, Color.WHITE);
            canvas.drawImage(0, TITLE_HEIGHT, image);
            lastImageHash = image.hashCode();
            String playerName = manager.getPlayerName();
            print("Image was posted by " + playerName);
            drawTitle(canvas, "Imgur");
        }
    }

    public void accept(CommandContext context) throws CommandException {
        if (context.matches("load")) {
            if (context.argsLength() < 1) {
                throw new CommandException("Imgur ID required");
            }
            
            String id = context.getJoinedStrings(0);
            if (!id.matches("^[A-Za-z0-9\\-]+$")) {
                throw new CommandException("Invalid Imgur ID entered");
            }
            
            if (manager.request(id, getPlayer())) {
                MapPad.print(getPlayer(), "Loading " + id + "...");
            } else {
                throw new CommandException("Please wait a few seconds and try again.");
            }
        } else if (context.matches("help")) {
            print("All images posted here are seen by everyone. Use >load # where # is the ID of an image hosted on Imgur.com. The ID is in the URL once you've uploaded an image.");
        } else {
            throw new CommandException("Unknown command! Try >help");
        }
    }

    public void quit() {
    }
    
    private static class ImgurFetcher implements Runnable {
        
        private String id;
        private ImgurRequestManager manager;
        private Player player;
        
        public ImgurFetcher(ImgurRequestManager manager, String id, Player player) {
            this.manager = manager;
            this.id = id;
            this.player = player;
        }
        
        public void run() {
            try {
                URL url = new URL("http://i.imgur.com/" + id + ".png");
                logger.info("ImgurBrowser: Loading " + url.toString() + " for " + player.getName());
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(10000);
                BufferedImage image = ImageIO.read(conn.getInputStream());
                manager.setImage(image.getScaledInstance(128, 128 - TITLE_HEIGHT, BufferedImage.SCALE_FAST), player);
                logger.info("ImgurBrowser: Loaded " + url.toString() + " successfully");
            } catch (FileNotFoundException e) {
                logger.log(Level.WARNING, "ImgurBrowser: Failed to load image from Imgur", e);
                MapPad.print(player, "Image " + id + " was not found on Imgur.com");
                manager.failRequest();
            } catch (IOException e) {
                logger.log(Level.WARNING, "ImgurBrowser: Failed to load image from Imgur", e);
                MapPad.print(player, "Image " + id + " could not load: " + e.getMessage());
                manager.failRequest();
            }
        }
        
    }
    
    private static class ImgurRequestManager {
        
        private boolean busy = false;
        private long lastSuccessfulRequest = 0;
        private Image image;
        private String playerName;
        
        public boolean request(String id, Player player) {
            synchronized(this) {
                if (busy || (System.currentTimeMillis() - lastSuccessfulRequest < 2000)) {
                    return false;
                } else {
                    busy = true;
                }
            }
            
            Thread thread = new Thread(new ImgurFetcher(this, id, player));
            thread.start();
            
            return true;
        }

        public Image getImage() {
            return image;
        }

        public String getPlayerName() {
            return playerName;
        }

        public void failRequest() {
            synchronized(this) {
                busy = false;
            }
        }

        public void setImage(Image newImage, Player player) {
            synchronized(this) {
                busy = false;
                lastSuccessfulRequest = System.currentTimeMillis();
            }
            
            image = newImage;
            playerName = player.getName();
        }
    }

}
