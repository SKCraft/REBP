/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

// $Id$


package com.sk89q.rebar.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;

import com.sk89q.rebar.config.types.BlockVector2dLoaderBuilder;
import com.sk89q.rebar.config.types.BooleanLoaderBuilder;
import com.sk89q.rebar.config.types.DoubleLoaderBuilder;
import com.sk89q.rebar.config.types.FloatLoaderBuilder;
import com.sk89q.rebar.config.types.IntegerLoaderBuilder;
import com.sk89q.rebar.config.types.LocationLoaderBuilder;
import com.sk89q.rebar.config.types.LongLoaderBuilder;
import com.sk89q.rebar.config.types.NodeLoaderBuilder;
import com.sk89q.rebar.config.types.StringLoaderBuilder;
import com.sk89q.rebar.config.types.Vector2dLoaderBuilder;
import com.sk89q.rebar.config.types.VectorLoaderBuilder;
import com.sk89q.rebar.util.EmptyIterator;
import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;

/**
 * Represents a configuration object.
 *
 * @author sk89q
 */
class ConfigurationObject {

    protected static final String ROOT = "";

    protected static final StringLoaderBuilder stringLB = new StringLoaderBuilder();
    protected static final BooleanLoaderBuilder boolLB = new BooleanLoaderBuilder();
    protected static final IntegerLoaderBuilder integerLB = new IntegerLoaderBuilder();
    protected static final LongLoaderBuilder longLB = new LongLoaderBuilder();
    protected static final DoubleLoaderBuilder doubleLB = new DoubleLoaderBuilder();
    protected static final FloatLoaderBuilder floatLB = new FloatLoaderBuilder();
    protected static final NodeLoaderBuilder nodeLB = new NodeLoaderBuilder();
    protected static final ListLoaderBuilder listLB = new ListLoaderBuilder();
    protected static final MapLoaderBuilder mapLB = new MapLoaderBuilder();

    protected Object root;

    /**
     * Construct the object with the given underlying object.
     *
     * @param object root object
     */
    public ConfigurationObject(Object object) {
        this.root = object;
    }

    /**
     * Gets a value at a location. The path along the way has to consist of maps
     * otherwise a null is returned prematurely. Parts of the path are delimited
     * by periods (.).
     *
     * @param path path to node (dot notation)
     * @return object, or the path does not exist, null
     */
    @Deprecated
    public Object getProperty(String path) {
        return get(parsePath(path));
    }

    /**
     * Gets a value at a location. The path along the way has to consist of maps
     * otherwise a null is returned prematurely. Parts of the path are delimited
     * by periods (.).
     *
     * @param path path to node (dot notation)
     * @return object, or the path does not exist, null
     */
    public Object get(String path) {
        return get(parsePath(path));
    }

    /**
     * Gets a value at a location. The path along the way has to consist of maps
     * otherwise a null is returned prematurely.
     *
     * @param parts parts of the path
     * @param delimeter path delimiter
     * @return object, or the path does not exist, null
     */
    @SuppressWarnings("unchecked")
    protected Object get(String[] parts) {
        if (parts.length == 0) {
            return root;
        }

        Map<Object, Object> node = (Map<Object, Object>) root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            if (o == null) {
                return null;
            } else if (i == parts.length - 1) {
                return o;
            }

            try {
                node = (Map<Object, Object>) o;
            } catch (ClassCastException e) {
                return null;
            }
        }

        return null;
    }

    /**
     * Get a value at a location, also parsing it.
     *
     * @param path path
     * @param loader loader to parse with
     * @return object or null
     *
     */
    public <V> V getOf(String path, Loader<V> loader) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        return loader.read(o);
    }

    /**
     * Get a value at a location, also parsing it.
     *
     * @param path path
     * @param agent loader to parse with (and builder to marshal with)
     * @param def default value
     * @return object or default
     *
     */
    public <V, K extends Loader<V> & Builder<V>> V getOf(String path, K agent,
            V def) {
        Object o = get(path);
        if (o == null) {
            Object res = agent.write(def);
            set(path, res);
            return def;
        }
        V val = agent.read(o);
        if (val == null) {
            Object res = agent.write(def);
            set(path, res);
            return def;
        }
        return val;
    }

    /**
     * Set the property at a location. This will override existing configuration
     * data to have it conform to key/value mappings. Parts of the path are
     * delimited by periods (.).
     *
     * @param path path to node (dot notation)
     * @param value value to set
     */
    @Deprecated
    public void setProperty(String path, Object value) {
        set(parsePath(path), value);
    }

    /**
     * Set the property at a location. This will override existing configuration
     * data to have it conform to key/value mappings. Parts of the path are
     * delimited by periods (.).
     *
     * @param path path to node (dot notation)
     * @param value value to set
     */
    public void set(String path, Object value) {
        set(parsePath(path), value);
    }

    /**
     * Set the property at a location. This will override existing configuration
     * data to have it conform to key/value mappings.
     *
     * @param parts parts of the path
     * @param value the value to set
     */
    @SuppressWarnings("unchecked")
    protected void set(String[] parts, Object value) {
        if (parts.length == 0) {
            throw new IllegalArgumentException("Invalid path");
        }

        Map<Object, Object> node = (Map<Object, Object>) root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            // Found our target!
            if (i == parts.length - 1) {
                node.put(parts[i], value);
                return;
            }

            if (o == null || !(o instanceof Map)) {
                // This will override existing configuration data!
                o = new HashMap<String, Object>();
                node.put(parts[i], o);
            }

            node = (Map<Object, Object>) o;
        }
    }

    /**
     * Set the property at a location. This will override existing configuration
     * data to have it conform to key/value mappings. Parts of the path are
     * delimited by periods (.).
     *
     * @param path path to node (dot notation)
     * @param builder builder to marshal the value
     * @param value the value to set
     *
     */
    public <V> void set(String path, Builder<V> builder, V value) {
        Object o = builder.write(value);
        set(path, o);
    }

    /**
     * Gets a string given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return string or null
     */
    public String getString(String path) {
        return getOf(path, stringLB);
    }

    /**
     * Gets a string given a path. Will return default value if not set. The
     * default value will also be set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return string or default
     */
    public String getString(String path, String def) {
        return getOf(path, stringLB, def);
    }

    /**
     * Gets an integer given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return integer or null
     */
    public Integer getInt(String path) {
        return getOf(path, integerLB);
    }

    /**
     * Gets an integer given a path. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return int or default
     */
    public int getInt(String path, int def) {
        return getOf(path, integerLB, def);
    }

    /**
     * Gets a long given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return long or null
     */
    public Long getLong(String path) {
        return getOf(path, longLB);
    }

    /**
     * Gets a double at a location. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return long or default
     */
    public long getLong(String path, long def) {
        return getOf(path, longLB, def);
    }

    /**
     * Gets a double given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return double or null
     */
    public Double getDouble(String path) {
        return getOf(path, doubleLB);
    }

    /**
     * Gets a double at a location. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return double or default
     */
    public double getDouble(String path, double def) {
        return getOf(path, doubleLB, def);
    }

    /**
     * Gets a float given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return float or null
     */
    public Float getFloat(String path) {
        return getOf(path, floatLB);
    }

    /**
     * Gets a float at a location. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return float or default
     */
    public float getFloat(String path, float def) {
        return getOf(path, floatLB, def);
    }

    /**
     * Gets a boolean given a path. May return null if not set.
     *
     * @param path path to node (dot notation)
     * @return boolean or null
     */
    public Boolean getBoolean(String path) {
        return getOf(path, boolLB);
    }

    /**
     * Gets a boolean given a path. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return boolean or default
     */
    public boolean getBoolean(String path, boolean def) {
        return getOf(path, boolLB, def);
    }

    /**
     * Gets a {@link ConfigurationNode}s given a path. May return null if not
     * set.
     *
     * @param path path to node (dot notation)
     * @return node or null
     */
    public ConfigurationNode getNode(String path) {
        return getOf(path, nodeLB);
    }

    /**
     * Gets a {@link ConfigurationNode}s given a path. Will return default value
     * if not set.
     *
     * @param path path to node (dot notation)
     * @param def default value
     * @return node or default
     */
    public ConfigurationNode getNode(String path, ConfigurationNode def) {
        return getOf(path, nodeLB, def);
    }

    /**
     * Get a vector at a path.
     *
     * @param path path to node (dot notation)
     * @return vector or null
     */
    @Deprecated
    public Vector getVector(String path) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        return new VectorLoaderBuilder().read(o);
    }

    /**
     * Get a vector at a path.
     *
     * @param path path to node (dot notation)
     * @param def default
     * @return vector or default
     */
    @Deprecated
    public Vector getVector(String path, Vector def) {
        Vector v = getVector(path);
        if (v == null) {
            setProperty(path, def);
            return def;
        }
        return v;
    }

    /**
     * Get a vector at a path.
     *
     * @param path path to node (dot notation)
     * @return vector or null
     */
    @Deprecated
    public Vector2D getVector2d(String path) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        return new Vector2dLoaderBuilder().read(o);
    }

    /**
     * Get a location at a path.
     *
     * @param path path to node (dot notation)
     * @param world world to put the location in
     * @return location or null
     */
    @Deprecated
    public Location getLocation(String path, World world) {
        Object o = get(path);
        if (o == null) {
            return null;
        }
        return new LocationLoaderBuilder(world).read(o);
    }

    /**
     * Gets a list of objects given a path.
     *
     * @param path path to node (dot notation)
     * @return list or null
     */
    @Deprecated
    public List<Object> getList(String path) {
        return getOf(path, listLB);
    }

    /**
     * Gets a list of objects given a path. Will return default value if not
     * set.
     *
     * @param path path to node (dot notation)
     * @param def a default list
     * @return list or default
     */
    @Deprecated
    public List<Object> getList(String path, List<Object> def) {
        return getOf(path, listLB, def);
    }

    /**
     * Gets a list of objects given a path. Will return default value if not
     * set.
     *
     * @param path path to node (dot notation)
     * @param def an iterator to provide default entries with
     * @return list or default
     */
    @Deprecated
    public List<Object> getList(String path, Iterator<Object> def) {
        List<Object> list = getList(path);
        if (list == null) {
            list = new ArrayList<Object>();
            while (def.hasNext()) {
                list.add(def.next());
            }
            set(path, list);
        }
        return list;
    }

    /**
     * Gets a map of objects given a path.
     *
     * @param path path to node (dot notation)
     * @return map or null
     */
    @Deprecated
    public Map<Object, Object> getMap(String path) {
        return getOf(path, mapLB);
    }

    /**
     * Gets a map of objects given a path. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def an iterator to provide default entries with
     * @return map or default
     */
    @Deprecated
    public Map<Object, Object> getMap(String path,
            Iterator<Map.Entry<Object, Object>> def) {
        Map<Object, Object> map = getMap(path);
        if (map == null) {
            map = new HashMap<Object, Object>();
            while (def.hasNext()) {
                Map.Entry<Object, Object> entry = def.next();
                map.put(entry.getKey(), entry.getValue());
            }
            set(path, map);
        }
        return map;
    }

    /**
     * Gets a map of objects given a path. Will return default value if not set.
     *
     * @param path path to node (dot notation)
     * @param def a default map
     * @return map or default
     */
    @Deprecated
    public Map<Object, Object> getMap(String path, Map<Object, Object> def) {
        return getOf(path, mapLB, def);
    }

    /**
     * Fills a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param loader loader to use to create the structures
     * @param collection collection to fill
     * @return list of objects or unaffected given collection
     *
     */
    private <V, K extends Collection<V>> K nullableCollectionOf(String path,
            Loader<V> loader, K collection) {
        List<Object> objectList = getList(path);

        // We don't have a list at that path
        if (objectList == null) {
            // But maybe we have something other than a list, in case the
            // user forgot to specify a list.
            V parsed = getOf(path, loader);
            if (parsed != null) {
                collection.add(parsed);
                return collection;
            }

            // Nope, we've got nothing
            return null;
        }

        // Try to unmarshal each object
        for (Object o : objectList) {
            V parsed = loader.read(o);
            if (parsed != null) {
                collection.add(parsed);
            }
        }

        return collection;
    }

    /**
     * Fills a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param loader loader to use to create the structures
     * @param collection collection to fill
     * @return list of objects or unaffected given collection
     *
     */
    public <V, K extends Collection<V>> K collectionOf(String path,
            Loader<V> loader, K collection) {
        nullableCollectionOf(path, loader, collection);
        return collection;
    }

    /**
     * Fills a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param agent loader to parse with (and builder to marshal with)
     * @param collection collection to fill
     * @param def iterator to pull default values from
     * @return list of objects or default list
     *
     */
    public <V, K extends Collection<V>, E extends Loader<V> & Builder<V>> K collectionOf(
            String path, E agent, K collection, Iterator<V> def) {
        Collection<V> result = nullableCollectionOf(path, agent, collection);
        if (result == null) {
            // Since the value does not exist, let's make a default list to
            // put back into the configuration
            List<Object> objectList = new ArrayList<Object>();
            while (def.hasNext()) {
                collection.add(def.next());
                objectList.add(agent.write(def.next()));
            }
            set(path, objectList);
        }
        return collection;
    }

    /**
     * Sets a collection of structures.
     *
     * @param path path to node (dot notation)
     * @param builder factory to use to marshal the structures
     * @param list list to use
     *
     */
    public <V> void setCollectionOf(String path, Builder<V> builder,
            Collection<V> list) {
        List<Object> objects = new ArrayList<Object>();
        for (V entry : list) {
            objects.add(builder.write(entry));
        }
        set(path, objects);
    }

    /**
     * Gets a list of structures given a path. The list will not contain nulls.
     *
     * @param path path to node (dot notation)
     * @param loader loader to use to create the structures
     * @return list of objects or empty list
     *
     */
    public <V> List<V> listOf(String path, Loader<V> loader) {
        return collectionOf(path, loader, new ArrayList<V>());
    }

    /**
     * Gets a list of structures given a path. The list will not contain nulls.
     *
     * @param path path to node (dot notation)
     * @param agent loader to parse with (and builder to marshal with)
     * @param def an iterator to provide default entries with
     * @return list of objects or the default list
     *
     */
    public <V, K extends Loader<V> & Builder<V>> List<V> listOf(String path,
            K agent, Iterator<V> def) {
        return collectionOf(path, agent, new ArrayList<V>(), def);
    }

    /**
     * Gets a list of structures given a path. The list will not contain nulls.
     *
     * @param path path to node (dot notation)
     * @param agent loader to parse with (and builder to marshal with)
     * @param def a collection of default entries
     * @return list of objects or the default list
     *
     */
    public <V, K extends Loader<V> & Builder<V>> List<V> listOf(String path,
            K agent, Collection<V> def) {
        return listOf(path, agent, def.iterator());
    }

    /**
     * Gets a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param loader loader to use to create the structures
     * @return set of objects or empty set
     *
     */
    public <V> Set<V> setOf(String path, Loader<V> loader) {
        return collectionOf(path, loader,
                new HashSet<V>());
    }

    /**
     * Gets a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param agent loader to parse with (and builder to marshal with)
     * @param def an iterator to provide default entries with
     * @return set of objects or the default set
     *
     */
    public <V, K extends Loader<V> & Builder<V>> Set<V> setOf(String path,
            K agent, Iterator<V> def) {
        return collectionOf(path, agent, new HashSet<V>(), def);
    }

    /**
     * Gets a collection of structures given a path. The collection will not
     * contain nulls.
     *
     * @param path path to node (dot notation)
     * @param agent loader to parse with (and builder to marshal with)
     * @param def a collection of default entries
     * @return set of objects or the default set
     *
     */
    public <V, K extends Loader<V> & Builder<V>> Set<V> setOf(String path,
            K agent, Collection<V> def) {
        return setOf(path, agent, def.iterator());
    }

    /**
     * Get a list of keys (cast to strings) given a path.
     *
     * @param path path to node (dot notation)
     * @return list of keys
     */
    @SuppressWarnings("unchecked")
    public List<String> getKeys(String path) {
        Object o = get(path);
        if (o == null) {
            return new ArrayList<String>();
        } else if (o instanceof Map) {
            return new ArrayList<String>(((Map<String, Object>) o).keySet());
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Gets a list of strings with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of strings
     */
    public List<String> getStringList(String path, List<String> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<String>();
        }
        return listOf(path, stringLB, def.iterator());
    }

    /**
     * Gets a list of integers with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of integers
     */
    public List<Integer> getIntList(String path, List<Integer> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Integer>();
        }
        return listOf(path, integerLB, def.iterator());
    }

    /**
     * Gets a list of longs with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of longs
     */
    public List<Long> getLongList(String path, List<Long> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Long>();
        }
        return listOf(path, longLB, def.iterator());
    }

    /**
     * Gets a list of doubles with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of doubles
     */
    public List<Double> getDoubleList(String path, List<Double> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Double>();
        }
        return listOf(path, doubleLB, def.iterator());
    }

    /**
     * Gets a list of booleans with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of booleans
     */
    public List<Boolean> getBooleanList(String path, List<Boolean> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Boolean>();
        }
        return listOf(path, boolLB, def.iterator());
    }

    /**
     * Gets a list of configuration nodes.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of nodes
     */
    public List<ConfigurationNode> getNodeList(String path,
            List<ConfigurationNode> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<ConfigurationNode>();
        }
        return listOf(path, nodeLB, def.iterator());
    }

    /**
     * Gets a list of vectors with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of vectors
     */
    @Deprecated
    public List<Vector> getVectorList(String path, List<Vector> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Vector>();
        }
        return listOf(path, new VectorLoaderBuilder(), def.iterator());
    }

    /**
     * Gets a list of vectors with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of vectors
     */
    @Deprecated
    public List<Vector2D> getVector2dList(String path, List<Vector2D> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<Vector2D>();
        }
        return listOf(path, new Vector2dLoaderBuilder(), def.iterator());
    }

    /**
     * Gets a list of vectors with no null entries.
     *
     * @param path path to node (dot notation)
     * @param def list of default values
     * @return list of vectors
     */
    @Deprecated
    public List<BlockVector2D> getBlockVector2dList(String path,
            List<BlockVector2D> def) {
        if (def == null) { // Legacy compatibility
            def = new ArrayList<BlockVector2D>();
        }
        return listOf(path, new BlockVector2dLoaderBuilder(), def.iterator());
    }

    /**
     * Get a key/value {@link Map} given a path.
     *
     * @param path path to node (dot notation)
     * @param loader loader to parse the values with
     * @param map an empty map to store the entries in
     * @return map of objects or unmodified given map
     *
     */
    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> nullableKeyValueOf(String path,
            KeyValueLoader<K, V> loader, Map<K, V> map) {
        Object o = get(path);
        if (o != null && o instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o)
                    .entrySet()) {
                Map.Entry<K, V> parsed = loader.read(entry.getKey(),
                        entry.getValue());
                if (parsed != null) {
                    map.put(parsed.getKey(), parsed.getValue());
                }
            }

            return map;
        } else {
            return null;
        }
    }

    /**
     * Get a key/value {@link Map} given a path.
     *
     * @param path path to node (dot notation)
     * @param loader loader to parse the values with
     * @param map an empty map to store the entries in
     * @return map of objects or unmodified given map
     *
     */
    public <K, V> Map<K, V> keyValueOf(String path,
            KeyValueLoader<K, V> loader, Map<K, V> map) {
        nullableKeyValueOf(path, loader, map);
        return map;
    }

    /**
     * Get a key/value {@link Map} given a path.
     *
     * @param path path to node (dot notation)
     * @param agent loader to unmarshal the values with, and a builder to
     *            marshal the default
     * @param map an empty map to store the entries in
     * @param def iterator to get default values from
     * @return map of objects or default
     *
     */
    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> keyValueOf(
            String path, E agent, Map<K, V> map, Iterator<Map.Entry<K, V>> def) {
        Map<K, V> result = keyValueOf(path, agent, map);
        if (result == null) {
            // Since the value does not exist, let's make a default map to
            // put back into the configuration
            Map<Object, Object> objectMap = new HashMap<Object, Object>();
            while (def.hasNext()) {
                Map.Entry<K, V> entry = def.next();
                map.put(entry.getKey(), entry.getValue());
                Map.Entry<Object, Object> m = agent.write(entry.getKey(),
                        entry.getValue());
                objectMap.put(m.getKey(), m.getValue());
            }
            set(path, objectMap);
        }
        return map;
    }

    /**
     * Get a key/value {@link Map} given a path.
     *
     * @param path path to node (dot notation)
     * @param loader loader to parse the values with
     * @param map an empty map to store the entries in
     * @param def iterator to get default values from
     * @return map of objects or default
     *
     */
    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> keyValueOf(
            String path, E loader, Map<K, V> map, Map<K, V> def) {
        return keyValueOf(path, loader, map, def.entrySet().iterator());
    }

    /**
     * Sets a key/value of structures.
     *
     * @param path path to node (dot notation)
     * @param builder factory to use to marshal the structures
     * @param map map of data to set
     *
     */
    public <K, V> void setKeyValueOf(String path,
            KeyValueBuilder<K, V> builder, Map<K, V> map) {
        Map<Object, Object> objects = new HashMap<Object, Object>();
        for (Entry<K, V> entry : map.entrySet()) {
            Map.Entry<Object, Object> result = builder.write(entry.getKey(),
                    entry.getValue());
            objects.put(result.getKey(), result.getValue());
        }
        set(path, objects);
    }

    /**
     * Get a {@link HashMap} given a path.
     *
     * @param path path to node (dot notation)
     * @param loader loader to parse the values with
     * @return map of objects or empty map
     *
     */
    public <K, V> Map<K, V> mapOf(String path, KeyValueLoader<K, V> loader) {
        return keyValueOf(path, loader, new HashMap<K, V>());
    }

    /**
     * Get a {@link HashMap} given a path.
     *
     * @param path path to node (dot notation)
     * @param agent loader to unmarshal the values with, and a builder to
     *            marshal the default
     * @param def iterator to get default values from
     * @return map of objects or a map of values from the defaults iterator
     *
     */
    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> mapOf(
            String path, E agent, Iterator<Map.Entry<K, V>> def) {
        return keyValueOf(path, agent, new HashMap<K, V>(), def);
    }

    /**
     * Get a {@link HashMap} given a path.
     *
     * @param path path to node (dot notation)
     * @param agent loader to unmarshal the values with, and a builder to
     *            marshal the default
     * @param def map containing default values
     * @return map of objects or the default map
     *
     */
    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> mapOf(
            String path, E agent, Map<K, V> def) {
        return mapOf(path, agent, def.entrySet().iterator());
    }

    /**
     * Get a list of nodes at a location. If the map at the particular location
     * does not exist or it is not a map, null will be returned.
     *
     * @param path path to node (dot notation)
     * @return map of nodes
     *
     */
    public Map<String, ConfigurationNode> getNodes(String path) {
        return mapOf(path, PairedKeyValueLoaderBuilder.build(stringLB, nodeLB),
                new EmptyIterator<Map.Entry<String, ConfigurationNode>>());
    }

    /**
     * Adds a new node to the given path.
     *
     * @param path a path to set the node at
     * @return the node (that can be modified)
     */
    public ConfigurationNode setNode(String path) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        ConfigurationNode node = new ConfigurationNode(map);
        set(path, map);
        return node;
    }

    /**
     * Adds a new node to the given path.
     *
     * @param path a path to set the node at
     * @return the node (that can be modified)
     */
    @Deprecated
    public ConfigurationNode addNode(String path) {
        return setNode(path);
    }

    /**
     * Remove the property at a location. This will override existing
     * configuration data to have it conform to key/value mappings.
     *
     * @param parts parts of the path
     */
    @SuppressWarnings("unchecked")
    public void remove(String[] parts) {
        if (parts.length == 0) {
            throw new IllegalArgumentException("Invalid path");
        }

        Map<Object, Object> node = (Map<Object, Object>) root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            // Found our target!
            if (i == parts.length - 1) {
                node.remove(parts[i]);
                return;
            }

            node = (Map<Object, Object>) o;
        }
    }

    /**
     * Remove the property at a location. This will override existing
     * configuration data to have it conform to key/value mappings.
     *
     * @param path path
     */
    public void remove(String path) {
        remove(parsePath(path));
    }

    /**
     * Remove the property at a location. This will override existing
     * configuration data to have it conform to key/value mappings.
     *
     * @param path path
     */
    @Deprecated
    public void removeProperty(String path) {
        remove(path);
    }

    /**
     * Returns whether the property exists. The value may be null.
     *
     * @param parts parts of the path
     * @return true if it exists
     */
    @SuppressWarnings("unchecked")
    public boolean contains(String[] parts) {
        if (parts.length == 0) {
            return true;
        }

        Map<Object, Object> node = (Map<Object, Object>) root;

        for (int i = 0; i < parts.length; i++) {
            Object o = node.get(parts[i]);

            // Found our target!
            if (i == parts.length - 1) {
                return node.containsKey(parts[i]);
            }

            node = (Map<Object, Object>) o;
        }

        return false;
    }

    /**
     * Returns whether the property exists. The value may be null.
     *
     * @param path path
     * @return true if it exists
     */
    public boolean contains(String path) {
        return contains(parsePath(path));
    }

    protected static String[] parsePath(String path) {
        if (path.length() == 0) {
            return new String[0];
        } else {
            return path.split("\\.");
        }
    }
}