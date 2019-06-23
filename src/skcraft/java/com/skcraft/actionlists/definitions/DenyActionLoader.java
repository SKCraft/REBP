/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.actionlists.definitions;

import com.skcraft.actionlists.DefinitionException;
import com.sk89q.rebar.config.Loader;

public class DenyActionLoader implements Loader<DenyAction> {

    private final DenyAction instance = new DenyAction();

    @Override
    public DenyAction read(Object value) throws DefinitionException {
        return instance;
    }

}
