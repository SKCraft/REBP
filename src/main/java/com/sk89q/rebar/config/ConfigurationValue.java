/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config;

import static com.sk89q.rebar.config.ConfigurationObject.ROOT;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ConfigurationValue {

    private final ConfigurationObject configObject;

    public ConfigurationValue(Object object) {
        configObject = new ConfigurationObject(object);
    }

    public Object get() {
        return configObject.get(ROOT);
    }

    public <V> V getOf(Loader<V> loader) throws ConfigurationException {
        return configObject.getOf(ROOT, loader);
    }

    public <V, K extends Loader<V> & Builder<V>> V getOf(K agent, V def)
            throws ConfigurationException {
        return configObject.getOf(ROOT, agent, def);
    }

    public String getString() {
        return configObject.getString(ROOT);
    }

    public String getString(String def) {
        return configObject.getString(ROOT, def);
    }

    public Integer getInt() {
        return configObject.getInt(ROOT);
    }

    public int getInt(int def) {
        return configObject.getInt(ROOT, def);
    }

    public Long getLong() {
        return configObject.getLong(ROOT);
    }

    public long getLong(long def) {
        return configObject.getLong(ROOT, def);
    }

    public Double getDouble() {
        return configObject.getDouble(ROOT);
    }

    public double getDouble(double def) {
        return configObject.getDouble(ROOT, def);
    }

    public Float getFloat() {
        return configObject.getFloat(ROOT);
    }

    public float getFloat(float def) {
        return configObject.getFloat(ROOT, def);
    }

    public Boolean getBoolean() {
        return configObject.getBoolean(ROOT);
    }

    public boolean getBoolean(boolean def) {
        return configObject.getBoolean(ROOT, def);
    }

    public ConfigurationNode getNode() {
        return configObject.getNode(ROOT);
    }

    public ConfigurationNode getNode(ConfigurationNode def) {
        return configObject.getNode(ROOT, def);
    }

    public <V, K extends Collection<V>> K collectionOf(Loader<V> loader,
            K collection) throws ConfigurationException {
        return configObject.collectionOf(ROOT, loader, collection);
    }

    public <V, K extends Collection<V>, J extends Loader<V> & Builder<V>> K collectionOf(
            J agent, K collection, Iterator<V> def)
            throws ConfigurationException {
        return configObject.collectionOf(ROOT, agent, collection, def);
    }

    public <V> List<V> listOf(Loader<V> loader) throws ConfigurationException {
        return configObject.listOf(ROOT, loader);
    }

    public <V, K extends Loader<V> & Builder<V>> List<V> listOf(K agent,
            Iterator<V> def) throws ConfigurationException {
        return configObject.listOf(ROOT, agent, def);
    }

    public <V, K extends Loader<V> & Builder<V>> List<V> listOf(K agent,
            Collection<V> def) throws ConfigurationException {
        return configObject.listOf(ROOT, agent, def);
    }

    public <V> Set<V> setOf(Loader<V> loader)
            throws ConfigurationException {
        return configObject.setOf(ROOT, loader);
    }

    public <V, K extends Loader<V> & Builder<V>> Set<V> setOf(K agent,
            Iterator<V> def) throws ConfigurationException {
        return configObject.setOf(ROOT, agent, def);
    }

    public <V, K extends Loader<V> & Builder<V>> Set<V> setOf(K agent,
            Collection<V> def) throws ConfigurationException {
        return configObject.setOf(ROOT, agent, def);
    }

    public List<String> getKeys() {
        return configObject.getKeys(ROOT);
    }

    public List<String> getStringList(List<String> def) {
        return configObject.getStringList(ROOT, def);
    }

    public List<Integer> getIntList(List<Integer> def) {
        return configObject.getIntList(ROOT, def);
    }

    public List<Long> getLongList(List<Long> def) {
        return configObject.getLongList(ROOT, def);
    }

    public List<Double> getDoubleList(List<Double> def) {
        return configObject.getDoubleList(ROOT, def);
    }

    public List<Boolean> getBooleanList(List<Boolean> def) {
        return configObject.getBooleanList(ROOT, def);
    }

    public List<ConfigurationNode> getNodeList(List<ConfigurationNode> def) {
        return configObject.getNodeList(ROOT, def);
    }

    public <K, V> Map<K, V> keyValueOf(KeyValueLoader<K, V> loader,
            Map<K, V> map) {
        return configObject.keyValueOf(ROOT, loader, map);
    }

    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> keyValueOf(
            E agent, Map<K, V> map, Iterator<Entry<K, V>> def) {
        return configObject.keyValueOf(ROOT, agent, map, def);
    }

    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> keyValueOf(
            E loader, Map<K, V> map, Map<K, V> def) {
        return configObject.keyValueOf(ROOT, loader, map, def);
    }

    public <K, V> Map<K, V> mapOf(KeyValueLoader<K, V> loader) {
        return configObject.mapOf(ROOT, loader);
    }

    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> mapOf(
            E agent, Iterator<Entry<K, V>> def) {
        return configObject.mapOf(ROOT, agent, def);
    }

    public <K, V, E extends KeyValueLoader<K, V> & KeyValueBuilder<K, V>> Map<K, V> mapOf(
            E agent, Map<K, V> def) {
        return configObject.mapOf(ROOT, agent, def);
    }

    public Map<String, ConfigurationNode> getNodes() {
        return configObject.getNodes(ROOT);
    }
}
