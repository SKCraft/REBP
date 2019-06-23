/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.WeakHashMap;
import java.util.logging.Level;

@Log
public final class ConfigurationMapper {

    private final ObjectMapper mapper = new ObjectMapper();
    private final WeakHashMap<Class<?>, Object> bound = new WeakHashMap<Class<?>, Object>();
    private final File dir;

    public ConfigurationMapper(File dir) {
        this.dir = dir;
    }

    public File getPath(Class<?> cls) {
        return new File(dir, cls.getCanonicalName() + ".json");
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T load(Class<T> cls) {
        T object = (T) bound.get(cls);
        if (object != null) {
            return object;
        } else {
            try {
                object = mapper.readValue(getPath(cls), cls);
            } catch (IOException e) {
                try {
                    object = cls.newInstance();
                } catch (InstantiationException | IllegalAccessException e1) {
                    throw new RuntimeException("Failed to load configuration", e1);
                }
            }
            bound.put(cls, object);
            write(object);
            return object;
        }
    }

    public synchronized void write(Object object) {
        File path = getPath(object.getClass());
        path.getParentFile().mkdirs();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(path, object);
        } catch (IOException e) {
            log.log(Level.WARNING, "Failed to write " + object.getClass().getCanonicalName() + " to " + path.getAbsolutePath(), e);
        }
    }

}
