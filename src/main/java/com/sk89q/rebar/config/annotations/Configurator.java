/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.annotations;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.ConfigurationException;
import com.sk89q.rebar.config.ConfigurationNode;
import com.sk89q.rebar.config.KeyValueBuilder;
import com.sk89q.rebar.config.Loader;

/**
 * Gets and sets configuration values on an object based on annotations
 * defined on that object.
 *
 * @author sk89q
 */
public class Configurator {

    private final Map<Class<?>, Object> instanceCache = new HashMap<Class<?>, Object>();

    /**
     * Register an instance of an object that will be used for loader/builders.
     *
     * @param object object to register
     */
    public void registerInstance(Object object) {
        instanceCache.put(object.getClass(), object);
    }

    /**
     * Load the settings defined in the given object.
     *
     * @param object object to update
     * @param from node to read from
     * @throws ConfigurationException thrown on any error
     */
    public void load(Object object, ConfigurationNode from) throws ConfigurationException {
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getFields()) {
            try {
                Setting setting = field.getAnnotation(Setting.class);
                if (setting != null) {
                    field.setAccessible(true);
                    loadField(object, clazz, field, setting, from);
                }
            } catch (Throwable t) {
                throw new ConfigurationException(
                        "Failed to process @Setting annotations of class "
                                + clazz.getCanonicalName(), t);
            }
        }
    }

    private void loadField(Object object, Class<?> clazz, Field field, Setting setting,
            ConfigurationNode node) throws ConfigurationException,
            IllegalArgumentException, IllegalAccessException, InstantiationException {

        String path = getSettingPath(field, setting);

        Object value = node.get(path);
        Of of;

        // Check if the setting is required
        if (setting.required() && value == null) {
            throw new ConfigurationException("Required field '" + path
                    + "' not defined");
        }

        if (handleLoad(field, object, setting, value) == true) {
            return;
        // collectionOf()
        /*} else if ((collectionOf = field.getAnnotation(CollectionOf.class)) != null) {
            Collection<Object> collectionValue = collectionOf.type().newInstance();
            Iterator<Object> it = value != null ? ((Collection<Object>) value).iterator() : emptyList;
            Object agent = newInstance(collectionOf.value());
            value = collectionOf(node, path, agent, collectionValue, it);
        // keyValueOf()
        } else if ((keyValueOf = field.getAnnotation(KeyValueOf.class)) != null) {
            Map<Object, Object> mapValue = keyValueOf.type().newInstance();
            Iterator<Map.Entry<Object, Object>> it = value != null ? ((Map<Object, Object>) value).entrySet().iterator() : emptyMap;
            value = keyValueOf(node, path, newInstance(keyValueOf.value()), mapValue, it);*/
        // getOf()
        } else if ((of = field.getAnnotation(Of.class)) != null) {
            value = getOf(node, path, of.value());
        }

        if (value != null) {
            field.set(object, value);
        }
    }

    /**
     * Custom handling of a {@link Setting} field.
     *
     * @param field the field
     * @param object the object being configured
     * @param setting the setting
     * @param value the value of the field in the object
     * @return true to skip further processing
     * @throws ConfigurationException thrown on load failure
     */
    protected boolean handleLoad(Field field, Object object, Setting setting, Object value)
            throws ConfigurationException {
        return false;
    }

    /**
     * Save the settings defined in the given object to a node.
     *
     * @param object object to read
     * @param to node to save to
     * @throws ConfigurationException thrown on any error
     */
    public void save(Object object, ConfigurationNode to) throws ConfigurationException {
        Class<?> clazz = object.getClass();

        for (Field field : clazz.getFields()) {
            try {
                Setting setting = field.getAnnotation(Setting.class);
                if (setting != null) {
                    field.setAccessible(true);
                    saveField(object, clazz, field, setting, to);
                }
            } catch (Throwable t) {
                throw new ConfigurationException(
                        "Failed to process @Setting annotations of class "
                                + clazz.getCanonicalName(), t);
            }
        }
    }

    private void saveField(Object object, Class<?> clazz, Field field, Setting setting,
            ConfigurationNode node) throws ConfigurationException,
            IllegalArgumentException, IllegalAccessException, InstantiationException {

        String path = getSettingPath(field, setting);
        Object value = field.get(object);

        CollectionOf collectionOf;
        KeyValueOf keyValueOf;
        Of of;

        if (value == null) {
            field.set(object, null);
            return;
        }

        if (handleSave(node, path, setting, value) == true) {
            return;
        // collectionOf()
        } else if ((collectionOf = field.getAnnotation(CollectionOf.class)) != null) {
            setCollectionOf(node, path, newInstance(collectionOf.value()), (Collection<?>) value);
        // keyValueOf()
        } else if ((keyValueOf = field.getAnnotation(KeyValueOf.class)) != null) {
            setKeyValueOf(node, path, newInstance(keyValueOf.value()), (Map<?, ?>) value);
        // getOf()
        } else if ((of = field.getAnnotation(Of.class)) != null) {
            set(node, path, newInstance(of.value()), value);
        } else {
            field.set(object, value);
        }
    }

    /**
     * Custom handling of a {@link Setting} field.
     *
     * @param node node to save the value on
     * @param path path to save onto
     * @param setting the setting
     * @param value the value of the field in the object
     * @return true to skip further processing
     */
    protected boolean handleSave(ConfigurationNode node, String path, Setting setting, Object value) {
        return false;
    }

    /**
     * Transform a field name to a node name (i.e. from houseType to
     * house-type).
     *
     * @param name name to transform
     * @return transformed name
     */
    public static String transformFieldName(final String name) {
        boolean findingUppercase = true;
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (Character.isUpperCase(ch)) {
                if (findingUppercase) {
                    result.append("-");
                    result.append(Character.toLowerCase(ch));
                    findingUppercase = false;
                } else {
                    result.append(Character.toLowerCase(ch));
                }
            } else {
                result.append(ch);
                findingUppercase = false;
            }
        }

        return result.toString();
    }

    /**
     * Get the name of the setting from a field and {@link Setting}.
     *
     * @param field field to read from
     * @param setting setting annotation
     * @return name of configuration
     */
    public static String getSettingPath(final Field field, final Setting setting) {
        if (setting != null) {
            String name = setting.value();
            if (name.length() > 0) {
                return name;
            }
        }

        return transformFieldName(field.getName());
    }

    /**
     * Get the name of the setting from a field.
     *
     * @param field field to read from
     * @return name of configuration
     */
    public static String getSettingPath(final Field field) {
        Setting setting = field.getAnnotation(Setting.class);
        return getSettingPath(field, setting);
    }

    private Object newInstance(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object instance = instanceCache.get(clazz);
        if (instance != null) {
            return instance;
        }

        instance = clazz.newInstance();
        instanceCache.put(clazz, instance);
        return instance;
    }

    @SuppressWarnings("unchecked")
    private static <V> V getOf(ConfigurationNode node, String path, Object loader) {
        return node.getOf(path, (Loader<V>) loader);
    }

    @SuppressWarnings("unchecked")
    private static <V> void setCollectionOf(ConfigurationNode node, String path,
            Object builder, Collection<?> list) {
        node.setCollectionOf(path, (Builder<V>) builder, (Collection<V>) list);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> void setKeyValueOf(ConfigurationNode node, String path,
            Object builder, Map<?, ?> list) {
        node.setKeyValueOf(path, (KeyValueBuilder<K, V>) builder, (Map<K, V>) list);
    }

    @SuppressWarnings("unchecked")
    private static <V> void set(ConfigurationNode node, String path,
            Object builder, Object value) {
        node.set(path, (Builder<V>) builder, (V) value);
    }

}
