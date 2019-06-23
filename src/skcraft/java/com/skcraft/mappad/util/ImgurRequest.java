/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import javax.imageio.ImageIO;

public abstract class ImgurRequest extends AbstractImageRequest {
    
    private String id;
    
    public ImgurRequest(String id) {
        this.id = id;
    }
    
    public void run() {
        try {
            URL url = new URL("http://i.imgur.com/" + id + ".png");
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(10000);
            BufferedImage image = ImageIO.read(conn.getInputStream());
            onSuccess(image);
        } catch (IOException e) {
            onFail(e);
        }
    }
    
}
