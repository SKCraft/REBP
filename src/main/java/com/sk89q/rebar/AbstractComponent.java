/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import com.avaje.ebean.EbeanServer;

public abstract class AbstractComponent implements Component {

    private List<Object> configurations = new ArrayList<Object>();

    @Override
    public void load() {
    }

    @Override
    public Collection<Class<?>> getEntities() {
        return new ArrayList<Class<?>>();
    }

    public EbeanServer getDatabase() {
        return Rebar.getInstance().getDatabase();
    }

    @Override
    public void reload() {
        reloadConfiguration();
    }

    public void reloadConfiguration() {
        for (Object obj : configurations) {
            Rebar.getInstance().populateConfig(obj);
        }
    }

    public <T> T configure(T config) {
        configurations.add(config);
        Rebar.getInstance().populateConfig(config);
        return config;
    }

    protected static Logger createLogger(Class<?> cls) {
        return Logger.getLogger(cls.getCanonicalName());
    }

}
