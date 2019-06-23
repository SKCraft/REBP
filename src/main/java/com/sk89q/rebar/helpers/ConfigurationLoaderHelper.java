/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import com.sk89q.rebar.Component;
import com.sk89q.rebar.ComponentLoader;
import com.sk89q.rebar.LoaderException;
import com.sk89q.rebar.LoaderHelper;
import com.sk89q.rebar.Rebar;

public class ConfigurationLoaderHelper implements LoaderHelper {

    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarchy, Field field) throws LoaderException {
        
        Class<?> dataType = field.getType();

        try {
            // Link component
            if (field.isAnnotationPresent(PopulateConfig.class)) {
                Object configObj = dataType.newInstance();
                Rebar.getInstance().populateConfig(configObj);
                field.setAccessible(true);
                field.set(component, configObj);
            }
        } catch (IllegalArgumentException e) {
            throw new LoaderException("Failed to populate configuration on " + field.getName() + " for "
                    + component.getClass().getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new LoaderException("Failed to populate configuration on " + field.getName() + " for "
                    + component.getClass().getCanonicalName(), e);
        } catch (InstantiationException e) {
            throw new LoaderException("Failed to populate configuration on " + field.getName() + " for "
                    + component.getClass().getCanonicalName(), e);
        }
    }

    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarchy)
            throws LoaderException {
    }

    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarch, Method method)
            throws LoaderException {
    }

}
