/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComponentLoader {
    private static final Logger logger = Logger.getLogger(ComponentLoader.class.getCanonicalName());
    
    private Map<Class<? extends Component>, Component> components =
            new LinkedHashMap<Class<? extends Component>, Component>();
    private List<Component> needInit = new ArrayList<Component>();
    private Set<Class<? extends LoaderHelper>> autoLoadedHelpers =
            new HashSet<Class<? extends LoaderHelper>>();
    private List<LoaderHelper> helpers = new ArrayList<LoaderHelper>();
    
    public synchronized <T extends Component> T load(Class<? extends T> componentClass)
            throws LoaderException {
        return load(componentClass, new HashSet<Class<? extends Component>>());
    }
    
    @SuppressWarnings("unchecked")
    public synchronized <T extends Component> T load(Class<? extends T> componentClass,
            Set<Class<? extends Component>> loadHierarchy)
            throws LoaderException {
        
        if (isLoaded(componentClass)) {
            return (T) components.get(componentClass);
        }
        
        if (loadHierarchy.contains(componentClass.getCanonicalName())) {
            throw new RecursiveLoadException();
        }
        
        loadHierarchy.add(componentClass);
        
        // Load required components
        for (Annotation annotation : componentClass.getAnnotations()) {
            if (annotation instanceof RequiresComponent) {
                for (Class<? extends Component> clazz : ((RequiresComponent) annotation).value()) {
                    load(clazz, loadHierarchy);
                }
            }
        }
        
        T component = null;
        
        try {
            Constructor<? extends Component> constr = componentClass.getConstructor();
            component = (T) constr.newInstance();
            component.load();
        } catch (IllegalArgumentException e) {
            throw new LoaderException(e);
        } catch (InstantiationException e) {
            throw new LoaderException(e);
        } catch (IllegalAccessException e) {
            throw new LoaderException(e);
        } catch (InvocationTargetException e) {
            throw new LoaderException(e);
        } catch (SecurityException e) {
            throw new LoaderException(e);
        } catch (NoSuchMethodException e) {
            throw new LoaderException(e);
        }
        
        processHelpers(component, loadHierarchy);
        
        needInit.add(component);
        
        components.put(componentClass, component);
        return component;
    }
    
    public void processHelpers(Object obj) throws LoaderException {
        processHelpers(obj, new HashSet<Class<? extends Component>>());
    }
    
    public void processHelpers(Object obj, Set<Class<? extends Component>> loadHierarchy)
            throws LoaderException {
        
        for (Field field : obj.getClass().getDeclaredFields()) {
            autoLoadHelpers(field);
            
            for (LoaderHelper helper : helpers) {
                helper.process(this, obj, loadHierarchy, field);
            }
        }
        
        for (Method method : obj.getClass().getDeclaredMethods()) {
            autoLoadHelpers(method);
            
            for (LoaderHelper helper : helpers) {
                helper.process(this, obj, loadHierarchy, method);
            }
        }
        
        for (LoaderHelper helper : helpers) {
            helper.process(this, obj, loadHierarchy);
        }
    }
    
    private void autoLoadHelpers(AnnotatedElement element) throws LoaderException {
        
        for (Annotation annotation : element.getAnnotations()) {
            Class<?> annotationCls = annotation.annotationType();
            if (annotation.annotationType().isAnnotationPresent(LoaderHelperProcessor.class)) {
                LoaderHelperProcessor processor = annotationCls.getAnnotation(LoaderHelperProcessor.class);
                LoaderHelper loadedHelper = registerHelper(processor.value());
                if (loadedHelper != null) {
                    logger.info("Auto-loaded loader helper " + processor.value().getCanonicalName());
                }
            }
        }
    }
    
    public synchronized <T extends Component> T install(T component) {
        components.put(component.getClass(), component);
        return component;
    }
    
    public synchronized boolean isLoaded(Class<? extends Component> componentClass) {
        return components.containsKey(componentClass);
    }
    
    public synchronized void registerHelper(LoaderHelper loaderHelper) {
        helpers.add(loaderHelper);
    }
    
    public synchronized LoaderHelper registerHelper(Class<? extends LoaderHelper> loaderHelper)
            throws LoaderException {
        
        if (autoLoadedHelpers.contains(loaderHelper)) {
            return null;
        }
        
        try {
            LoaderHelper helper = loaderHelper.newInstance();
            helpers.add(helper);
            autoLoadedHelpers.add(loaderHelper);
            return helper;
        } catch (InstantiationException e) {
            throw new LoaderException("Failed to auto-load loader helper", e);
        } catch (IllegalAccessException e) {
            throw new LoaderException("Failed to auto-load loader helper", e);
        }
    }
    
    public synchronized void shutdown() {
        for (Component component : components.values()) {
            try {
                component.shutdown();
            } catch (Throwable t) {
                logger.log(Level.SEVERE,
                        "Failed to shutdown " + component.getClass().getName(), t);
            }
        }
        
        components.clear();
    }
    
    public synchronized boolean initialize() {
        boolean allSuccess = true;
        
        for (Component component : needInit) {
            try {
                component.initialize();
            } catch (Throwable t) {
                allSuccess = false;
                logger.log(Level.SEVERE,
                        "Failed to initialize " + component.getClass().getName(), t);
            }
        }
        needInit.clear();
        return allSuccess;
    }
    
    public synchronized void reload() {
        for (Component component : components.values()) {
            try {
                component.reload();
            } catch (Throwable t) {
                logger.log(Level.SEVERE,
                        "Failed to reload " + component.getClass().getName(), t);
            }
        }
    }
    
    public synchronized List<Class<?>> getEntities() {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (Entry<Class<? extends Component>, Component> component : components.entrySet()) {
            for (Class<?> entity : component.getValue().getEntities()) {
                classes.add(entity);
            }
        }
        return classes;
    }
    
    public synchronized Collection<Component> getLoaded() {
        return components.values();
    }
    
}
