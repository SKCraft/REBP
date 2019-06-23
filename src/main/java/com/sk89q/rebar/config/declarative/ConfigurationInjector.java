/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.declarative;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.ConfigurationBase;
import com.sk89q.rebar.config.DummyBuilder;
import com.sk89q.rebar.config.DummyKeyValueBuilder;
import com.sk89q.rebar.config.KeyValueLoader;
import com.sk89q.rebar.config.ListStructureType;
import com.sk89q.rebar.config.Loader;
import com.sk89q.rebar.util.EmptyIterator;

public class ConfigurationInjector {
    private static final Logger logger = Logger.getLogger(ConfigurationInjector.class.getCanonicalName());
    
    private Configuration config;
    
    public ConfigurationInjector(Configuration config) {
        this.config = config;
    }
    
    @SuppressWarnings("unchecked")
    public void inject(Object obj) {
        String prefix = "";
        
        if (obj.getClass().isAnnotationPresent(SettingBase.class)) {
            SettingBase base = obj.getClass().getAnnotation(SettingBase.class);
            prefix = base.value() + ".";
        }
        
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Setting.class)) {
                Setting setting = field.getAnnotation(Setting.class);
                Class<?> type = field.getType();
                String key = prefix + setting.value();

                try {
                    if (String.class.isAssignableFrom(type)) {
                        DefaultString def = field.getAnnotation(DefaultString.class);
                        field.setAccessible(true);
                        field.set(obj, config.getString(key, def == null ? null : def.value()));
                    
                    } else if (Integer.class.isAssignableFrom(type)) {
                        DefaultInt def = field.getAnnotation(DefaultInt.class);
                        field.setAccessible(true);
                        field.set(obj, config.getInt(key, def == null ? null : def.value()));
                    
                    } else if (Double.class.isAssignableFrom(type)) {
                        DefaultDouble def = field.getAnnotation(DefaultDouble.class);
                        field.setAccessible(true);
                        field.set(obj, config.getDouble(key, def == null ? null : def.value()));
                    
                    } else if (Boolean.class.isAssignableFrom(type)) {
                        DefaultBoolean def = field.getAnnotation(DefaultBoolean.class);
                        field.setAccessible(true);
                        field.set(obj, config.getBoolean(key, def == null ? null : def.value()));
                    
                    } else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
                        boolean wantSet = Set.class.isAssignableFrom(type);
                        ListType def = field.getAnnotation(ListType.class);
                        ListStructureType structDef = field.getAnnotation(ListStructureType.class);

                        field.setAccessible(true);
                        List<?> result = null;
                        
                        if (def != null) {
                            if (Integer.class.isAssignableFrom(def.value())) {
                                result = config.getIntList(key, new ArrayList<Integer>());
                            } else if (String.class.isAssignableFrom(def.value())) {
                                result = config.getStringList(key, new ArrayList<String>());
                            }
                        } else if (structDef != null) {
                            Loader<Object> factory = (Loader<Object>) structDef.value().newInstance();
                            EmptyIterator<Object> it = new EmptyIterator<Object>();
                            result = config.listOf(key, new DummyBuilder<Object>(factory), it);
                        }
                        
                        if (result != null) {
                            field.set(obj, wantSet ? new HashSet<Object>(result) : result);
                        }
                    
                    } else if (Map.class.isAssignableFrom(type)) {
                        MapType def = field.getAnnotation(MapType.class);
                        if (def == null)
                            continue;
                        field.setAccessible(true);
                        KeyValueLoader<Object, Object> factory = (KeyValueLoader<Object, Object>) def
                                .value().newInstance();
                        EmptyIterator<Map.Entry<Object, Object>> it = new EmptyIterator<Map.Entry<Object, Object>>();
                        field.set(obj, config.mapOf(key,
                                new DummyKeyValueBuilder<Object, Object>(
                                        factory), it));
                    }
                } catch (IllegalArgumentException e) {
                    logger.log(Level.WARNING, "Failed to inject configuration on "
                            + obj.getClass().getCanonicalName(), e);
                } catch (IllegalAccessException e) {
                    logger.log(Level.WARNING, "Failed to inject configuration on "
                            + obj.getClass().getCanonicalName(), e);
                } catch (InstantiationException e) {
                    logger.log(Level.WARNING, "Failed to inject configuration on "
                            + obj.getClass().getCanonicalName(), e);
                }
            }
        }
        
        if (obj instanceof ConfigurationBase) {
            ((ConfigurationBase) obj).populate(config);
        }
    }

    public void reload() throws IOException {
        config.load();
    }

}
