/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.rebar.util.BestResultsAggregator;
import com.sk89q.rebar.util.StringUtil;

public class ICConfigurationManager {
    
    private final Map<String, ICConfiguration> configurations;
    
    public ICConfigurationManager() {
        configurations = new HashMap<String, ICConfiguration>();
    }
    
    public void register(String id, ICFactory factory, Family family, String ... aliases) {
        ICConfiguration configuration = new ICConfiguration(id, factory, family);
        configurations.put(id.toLowerCase(), configuration);
        
        for (String alias : aliases) {
            configurations.put(alias.toLowerCase(), configuration);
        }
    }
    
    public ICConfiguration get(String id) {
        return configurations.get(id.toLowerCase());
    }
    
    public List<String> findSimiliarIDs(String id) {
        id = id.toLowerCase();
        
        BestResultsAggregator<String> results =
                new BestResultsAggregator<String>(5, BestResultsAggregator.Order.WEIGHT_LOWER);
        
        for (Map.Entry<String, ICConfiguration> entry : configurations.entrySet()) {
            String testLower = entry.getKey().toLowerCase();
            if (id.charAt(0) != testLower.charAt(0)) continue;
            int dist = StringUtil.getLevenshteinDistance(id, testLower.toLowerCase());
            if (dist > 10) continue;
            results.add(entry.getKey(), dist);
        }
        
        return results.getResults();
    }
    
    public Collection<ICConfiguration> getUniqueICs() {
        Map<String, ICConfiguration> unique = new HashMap<String, ICConfiguration>();

        for (Map.Entry<String, ICConfiguration> entry : configurations.entrySet()) {
            unique.put(entry.getValue().getId(), entry.getValue());
        }
        
        return unique.values();
    }

    public List<String> getAliases(ICConfiguration configuration) {
        List<String> aliases = new ArrayList<String>();

        for (Map.Entry<String, ICConfiguration> entry : configurations.entrySet()) {
            if (entry.getValue() == configuration) {
                aliases.add(entry.getKey().toUpperCase());
            }
        }
        
        return aliases;
    }

}
