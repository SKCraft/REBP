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
import com.sk89q.rebar.LazyPluginReference;
import com.sk89q.rebar.LoaderException;
import com.sk89q.rebar.LoaderHelper;
import com.sk89q.rebar.Rebar;

public class LinkerLoaderHelper implements LoaderHelper {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void process(ComponentLoader loader, Object component,
            Set<Class<? extends Component>> loadHierarchy, Field field) throws LoaderException {
        
        Class<?> dataType = field.getType();

        try {
            // Link component
            if (field.isAnnotationPresent(InjectComponent.class)) {
                if (Component.class.isAssignableFrom(dataType)) {
                    field.setAccessible(true);
                    field.set(component, load(loader, (Class<? extends Component>) dataType, loadHierarchy));
                } else {
                    throw new LoaderException("Non-component link type with "
                            + dataType.getCanonicalName() + " for "
                            + component.getClass().getCanonicalName());
                }
                
                // Link Bukkit plugin
                } else if (field.isAnnotationPresent(InjectPlugin.class)) {
                    InjectPlugin link = field.getAnnotation(InjectPlugin.class);
                    Class<?> linkClass = link.value();

                    if (!LazyPluginReference.class.isAssignableFrom(dataType)) {
                        throw new LoaderException("LinkBukkitPlugin must be used with LazyPluginLink for "
                                + component.getClass().getCanonicalName());
                    }
                    
                    if (linkClass == void.class) {
                        throw new LoaderException("Cannot link to void.class for "
                                + component.getClass().getCanonicalName());
                    }
                    
                    field.setAccessible(true);
                    field.set(component, new LazyPluginReference(linkClass));
                
                // Link service
                } else if (field.isAnnotationPresent(InjectService.class)) {
                    Object provider = Rebar.getInstance().getServiceManager().load(dataType);
                    if (provider == null) {
                        throw new LoaderException("Could find a service for " + dataType.getCanonicalName());
                    }
                    field.setAccessible(true);
                    field.set(component, provider);
            }
        } catch (IllegalArgumentException e) {
            throw new LoaderException("Failed to link field " + field.getName() + " for "
                    + component.getClass().getCanonicalName(), e);
        } catch (IllegalAccessException e) {
            throw new LoaderException("Failed to link field " + field.getName() + " for "
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
    
    private <T extends Component> T load(ComponentLoader loader,
            Class<T> componentClass, Set<Class<? extends Component>> loadHierarchy)
            throws LoaderException {
        return loader.load(componentClass, loadHierarchy);
    }

}
