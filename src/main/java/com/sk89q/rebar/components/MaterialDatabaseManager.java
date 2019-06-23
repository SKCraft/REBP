/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.components;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.util.MaterialDatabase;

public class MaterialDatabaseManager extends AbstractComponent {

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void reloadConfiguration() {
        super.reloadConfiguration();

        MaterialDatabase.reload();
    }

}
