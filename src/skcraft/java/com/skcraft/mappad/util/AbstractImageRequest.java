/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.mappad.util;

import java.awt.Image;

public abstract class AbstractImageRequest implements ImageRequest {

    public void onFail() {
    }

    public void onSuccess(Image image) {
    }

}
