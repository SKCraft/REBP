/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sk89q.rebar.AbstractComponent;

public class URLShortener extends AbstractComponent {

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public Collection<Class<?>> getEntities() {
        List<Class<?>> entities = new ArrayList<Class<?>>();
        entities.add(ShortenedURL.class);
        return entities;
    }

    public String shorten(String url) {

        try {
            getDatabase().beginTransaction();

            ShortenedURL shortened = new ShortenedURL(url);
            getDatabase().insert(shortened);

            getDatabase().commitTransaction();

            return "http://skcraft.com/s/" + shortened.getId();
        } finally {
            getDatabase().endTransaction();
        }
    }

}
