/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

public interface LoaderHelper {
    
    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarchy) throws LoaderException;
    
    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarch, Method method) throws LoaderException;
    
    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarch, Field field) throws LoaderException;

}
