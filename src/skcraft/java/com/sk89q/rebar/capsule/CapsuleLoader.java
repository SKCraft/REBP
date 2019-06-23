/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.capsule;

import com.sk89q.rebar.AbstractComponent;
import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.capsule.config.ConfigurationMapper;
import groovy.lang.GroovyClassLoader;
import lombok.extern.java.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@Log
public class CapsuleLoader extends AbstractComponent {

    private final Map<String, Capsule> loaded = new HashMap<>();
    private GroovyClassLoader gcl;
    private File capsulesDir;
    private ConfigurationMapper configMapper;
    private CapsuleConfig config;

    @Override
    public void initialize() {
        configMapper = new ConfigurationMapper(new File("capsule/config"));
        config = configMapper.load(CapsuleConfig.class);
        capsulesDir = new File("capsule/capsules");
        ClassLoader parent = getClass().getClassLoader();
        gcl = new GroovyClassLoader(parent);
        gcl.addClasspath(capsulesDir.getAbsolutePath());
        gcl.setShouldRecompile(true);

        Rebar.getInstance().registerCommands(CapsuleRootCommand.class, this);

        for (String name : config.getCapsules()) {
            try {
                load(name);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load the capsule '" + name + "'", e);
            }
        }
    }

    @Override
    public void shutdown() {
    }

    public Capsule load(String name) throws IOException {
        if (loaded.containsKey(name)) {
            return loaded.get(name);
        } else {
            File file = getScript(name);
            Capsule capsule;

            try {
                CapsuleLoader.log.info("CapsuleLoader: Loading " + file.getAbsolutePath() + "...");
                Class<?> clazz = gcl.loadClass(name, true, false, true);
                capsule = (Capsule) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IOException("Failed to load capsule", e);
            } catch (ClassNotFoundException e) {
                throw new IOException("Failed to find capsule", e);
            }

            CapsuleLoader.log.info("CapsuleLoader: Initializing " + capsule.getClass().getCanonicalName() + "...");

            try {
                capsule.initialize();
            } catch (Throwable t) {
                try {
                    capsule.shutdown();
                } catch (Throwable t2) {}
                throw new IOException("Failed to initialize capsule", t);
            }

            loaded.put(name, capsule);
            return capsule;
        }
    }

    public boolean unload(String name) {
        if (loaded.containsKey(name)) {
            Capsule capsule = loaded.get(name);
            try {
                capsule.shutdown();
            } catch (Throwable t) {
                CapsuleLoader.log.log(Level.WARNING, "CapsuleLoader: Failed to call capsule's shutdown() of " +
                        capsule.getClass().getCanonicalName(), t);
            }
            loaded.remove(name);
            return true;
        } else {
            return false;
        }
    }

    public void setLoadOnBoot(String name, boolean load) {
        Set<String> capsules = config.getCapsules();
        if (load && !capsules.contains(name)) {
            capsules.add(name);
            configMapper.write(config);
        } else if (!load && capsules.contains(name)) {
            capsules.remove(name);
            configMapper.write(config);
        }
    }

    private File getScript(String name) {
        return new File("capsules/" + name + ".groovy");
    }

}
