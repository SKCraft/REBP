/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.config.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sk89q.rebar.config.Builder;
import com.sk89q.rebar.config.Loader;
import com.sk89q.rebar.util.MapBuilder.ObjectMapBuilder;
import com.sk89q.rebar.util.MaterialDatabase;
import com.sk89q.rebar.util.MaterialPattern;

public class MaterialPatternLoaderBuilder implements Loader<MaterialPattern>, Builder<MaterialPattern> {

    private Logger logger = Logger.getLogger(MaterialPatternLoaderBuilder.class
            .getCanonicalName());
    private static final Pattern rangePattern = Pattern.compile("^([0-9]+)(?:\\-|\\.\\.)([0-9]+)$");
    private final MaterialDatabase materialDb;

    public MaterialPatternLoaderBuilder(MaterialDatabase materialDb) {
        this.materialDb = materialDb;
    }

    public MaterialPatternLoaderBuilder() {
        this.materialDb = MaterialDatabase.getInstance();
    }

    @Override
    public Object write(MaterialPattern value) {
        if (!value.hasDataFilter()) {
            return value.getMaterial();
        } else {
            List<Object> dataList = new ArrayList<Object>();
            int[] dataRange = value.getDataRange();
            for (int i = 0; i < dataRange.length; i++) {
                int val = dataRange[i];

                // Range
                if (val == -1) {
                    int min = dataRange[i + 1];
                    int max = dataRange[i + 2];
                    i += 2;
                    dataList.add(min + ".." + max);
                } else {
                    dataList.add(val);
                }
            }
            return new ObjectMapBuilder().put(value.getMaterial(), dataList).map();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MaterialPattern read(Object value) {
        if (value == null) {
            logger.warning("Unknown material (null value)");
            return null;

        } else if (value instanceof Map) {
            Map<Object, Object> structure = (Map<Object, Object>) value;

            if (structure.size() != 1) {
                logger.warning("Material in format (mat:data) must have only one entry");
                return null;
            }

            Object material = structure.keySet().iterator().next();

            if (material == null) {
                logger.warning("Invalid material in material:data format (null provided for material)");
                return null;
            }

            MaterialPattern pattern = fromMaterial(material);

            if (pattern == null) {
                return null; // Already handled an error
            }

            if (pattern.hasDataFilter()) {
                logger.warning("Material '" + material + "' already has data attached in the material database; can't add more");
                return pattern;
            }

            Object data = structure.get(material);

            if (data == null) {
                return pattern;
            } else if (data instanceof List) {
                for (Object dataEntry : (List<Object>) data) {
                    appendData(pattern, dataEntry);
                }
            } else {
                appendData(pattern, data);
            }

            return pattern;

        } else {
            return fromMaterial(value);
        }
    }

    private MaterialPattern fromMaterial(Object value) {
        if (value instanceof Integer) {
            Integer material = (Integer) value;

            if (material > -1) {
                return new MaterialPattern(material);
            } else {
                logger.warning("Invalid material " + material + " (must be a positive material number)");
                return null;
            }

        } else if (value instanceof String) {
            String name = (String) value;

            if (materialDb == null) {
                logger.warning("Unknown material " + name + " (no material name database is in use)");
                return null;
            }

            MaterialPattern pattern = materialDb.getPattern(name);

            if (pattern == null) {
                logger.warning("Unknown material " + name + " (not recognized)");
                return null;
            }

            return pattern;
        } else {
            logger.warning("Unknown material (unknown data type)");
            return null;
        }
    }

    private void appendData(MaterialPattern pattern, Object value) {
        if (value instanceof Integer) {
            Integer data = (Integer) value;

            if (data > -1) {
                pattern.filterData(data);
            } else {
                logger.warning("Invalid data value " + data
                        + " (must be a positive number) for material of ID "
                        + pattern.getMaterial());
            }
        } else if (value instanceof String) {
            String str = (String) value;
            Matcher m = rangePattern.matcher(str);

            if (m.matches()) {
                int min = Integer.parseInt(m.group(1));
                int max = Integer.parseInt(m.group(2));

                pattern.filterDataRange(min, max);
            } else {
                logger.warning("Invalid data value '" + str
                        + "' (expected a range as a..b) for material of ID "
                        + pattern.getMaterial());
            }
        }
    }

}
