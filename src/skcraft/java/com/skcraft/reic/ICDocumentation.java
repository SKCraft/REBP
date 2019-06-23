/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.skcraft.reic;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;

public class ICDocumentation {

    private String summary = "";
    private String allInputs;
    private final Map<Integer, String> inputs = new HashMap<Integer, String>();
    private final Map<Integer, String> outputs = new HashMap<Integer, String>();
    private final Map<Integer, String> parameters = new HashMap<Integer, String>();
    
    public ICDocumentation summary(String summary) {
        this.summary = summary;
        return this;
    }
    
    private ICDocumentation param(int num, String message) {
        parameters.put(num, message);
        return this;
    }
    
    public ICDocumentation param(String message) {
        param(parameters.size(), message);
        return this;
    }
    
    public ICDocumentation params(String message) {
        for (int i = parameters.size(); i < 4; i++) {
            param(i, message);
        }
        return this;
    }
    
    private ICDocumentation input(int num, String message) {
        inputs.put(num, message);
        return this;
    }
    
    public ICDocumentation input(String message) {
        input(inputs.size(), message);
        return this;
    }

    public ICDocumentation inputs(String message) {
        this.allInputs = message;
        return this;
    }
    
    private ICDocumentation output(int num, String message) {
        outputs.put(num, message);
        return this;
    }
    
    public ICDocumentation output(String message) {
        output(outputs.size(), message);
        return this;
    }
    
    private void appendFields(StringBuilder str, Map<Integer, String> map, String name, ChatColor color) {
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            str.append("\n");
            str.append(color);
            str.append(name);
            str.append(" #");
            str.append(entry.getKey());
            str.append(": ");
            str.append(ChatColor.WHITE);
            str.append(entry.getValue());
        }
    }

    public String toString() {
        StringBuilder str = new StringBuilder(summary);
        appendFields(str, parameters, "Param", ChatColor.BLUE);
        appendFields(str, inputs, "Input", ChatColor.GREEN);
        if (allInputs != null) {
            appendFields(str, inputs, "Input", ChatColor.GREEN);
            str.append("Inputs: ");
            str.append(ChatColor.WHITE);
            str.append(allInputs);
        }
        appendFields(str, outputs, "Output", ChatColor.LIGHT_PURPLE);
        return str.toString();
    }
    
}
