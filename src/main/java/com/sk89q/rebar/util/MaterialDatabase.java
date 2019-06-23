/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sk89q.rebar.Rebar;
import com.sk89q.rebar.config.Configuration;
import com.sk89q.rebar.config.PairedKeyValueLoaderBuilder;
import com.sk89q.rebar.config.types.LowercaseStringLoaderBuilder;
import com.sk89q.rebar.config.types.MaterialPatternLoaderBuilder;

public class MaterialDatabase {

    private static MaterialDatabase instance;

    private static final Logger logger = Logger.getLogger(MaterialDatabase.class.getCanonicalName());
    private final File file;
    private Map<String, MaterialPattern> patterns = new HashMap<String, MaterialPattern>();

    public static MaterialDatabase getInstance() {
        if (instance == null) {
            instance = new MaterialDatabase();
        }
        return instance;
    }

    private MaterialDatabase() {
        file = new File(Rebar.getInstance().getDataFolder(), "materials.yml");

        try {
            loadMaterials();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load materials database", e);
        }
    }

    public void loadMaterials() throws IOException {
        if (!file.exists()) {
            return;
        }

        Configuration config = new Configuration(file);
        config.load();

        PairedKeyValueLoaderBuilder<String, MaterialPattern> loader =
                PairedKeyValueLoaderBuilder.build(
                        new LowercaseStringLoaderBuilder(),
                        new MaterialPatternLoaderBuilder(null));
        patterns = config.mapOf("materials", loader);
    }

    public MaterialPattern getPattern(String name) {
        return patterns.get(name.toLowerCase());
    }

    public static void reload() {
        try {
            getInstance().loadMaterials();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to load materials database", e);
        }
    }

}
