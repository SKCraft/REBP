/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.util;

import java.awt.Image;

interface ImageRequest extends Runnable {
    
    public void onFail(Throwable error);
    public void onSuccess(Image image);
    
}
