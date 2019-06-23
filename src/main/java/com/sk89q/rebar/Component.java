/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

import java.util.Collection;

public interface Component {
    
    public Collection<Class<?>> getEntities();

    public void load();

    public void initialize();

    public void reload();

    public void shutdown();
    
}
