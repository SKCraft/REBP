/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import java.util.HashMap;
import java.util.Map;

/**
 * A configuration node with various methods to access properties of it. The underlying
 * structure is a {@link Map}.
 *
 * @author sk89q
 */
public class ConfigurationNode extends ConfigurationObject implements Cloneable {

    public static final String ROOT = "";

    /**
     * Construct the node from the given map.
     *
     * @param root root node
     */
    public ConfigurationNode(Map<Object, Object> root) {
        super(root);
    }

    /**
     * Attempt to construct a node from the given object. If the object is
     * not a map, then the {@link ConfigurationNode} will consist of a new
     * empty map. This will not make a shallow copy of the map.
     *
     * @param object an object to construct a node from
     */
    public ConfigurationNode(Object object) {
        super(makeMap(object));
    }

    /**
     * Clear all nodes.
     */
    public void clear() {
        getUnderlyingMap().clear();
    }

    /**
     * Shallow copy the node.
     */
    @Override
    public ConfigurationNode clone() {
        return new ConfigurationNode(shallowClone(getUnderlyingMap()));
    }

    /**
     * Get the underlying map.
     *
     * @return map
     */
    @SuppressWarnings("unchecked")
    public Map<Object, Object> getUnderlyingMap() {
        return (Map<Object, Object>) get(ROOT);
    }

    /**
     * Used to coerce the given object into a map (if possible), otherwise
     * an empty map is returned.
     *
     * @param object object to try it on
     * @return a map
     */
    @SuppressWarnings("unchecked")
    private static Map<Object, Object> makeMap(Object object) {
        if (object instanceof Map) {
            return (Map<Object, Object>) object;
        } else {
            return new HashMap<Object, Object>();
        }
    }

    /**
     * Get the shallow clone of a map.
     *
     * @param original original map
     * @return cloned map
     */
    private static Map<Object, Object> shallowClone(Map<Object, Object> original) {
        Map<Object, Object> cloned = new HashMap<Object, Object>();
        for (Map.Entry<Object, Object> entry : original.entrySet()) {
            cloned.put(entry.getKey(), entry.getValue());
        }
        return cloned;
    }

}
