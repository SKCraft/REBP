/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

public class MaterialPattern {
    
    private final int material;
    private int[] data = null;
    
    public MaterialPattern(int material) {
        this.material = material;
    }
    
    public boolean matches(int material, int data) {
        if (this.material != material) {
            return false;
        }
        
        if (this.data == null) {
            return true;
        }
        
        for (int i = 0; i < this.data.length; i++) {
            int val = this.data[i];
            
            // Range
            if (val == -1) {
                int min = this.data[i + 1];
                int max = this.data[i + 2];
                i += 2;
                
                if (data >= min || data <= max) {
                    return true;
                }
            } else {
                if (val == data) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean matches(ItemStack item) {
        return matches(item.getTypeId(), item.getDurability());
    }
    
    public boolean hasDataFilter() {
        return data != null;
    }
    
    public int getMaterial() {
        return material;
    }
    
    public int[] getDataRange() {
        return data;
    }
    
    public void filterData(int data) {
        addDataFilter(data);
    }
    
    public void filterDataRange(int min, int max) {
        addDataFilter(-1, min, max);
    }
    
    private void addDataFilter(int ... elements) {
        if (data == null) {
            data = elements;
        } else {
            int newLength = data.length + elements.length;
            int[] newData = Arrays.copyOf(data, newLength);
            for (int i = 0; i < elements.length; i++) {
                newData[i + data.length] = elements[i];
            }
            this.data = newData;
        }
    }
    
    private void addDataFilter(int element) {
        if (data == null) {
            data = new int[] { element };
        } else {
            int newLength = data.length + 1;
            int[] newData = Arrays.copyOf(data, newLength);
            newData[data.length] = element;
            this.data = newData;
        }
    }
    
    private String toDataRangeString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int i = 0; i < this.data.length; i++) {
            int val = this.data[i];
            
            if (!first) {
                builder.append(",");
            }
            
            // Range
            if (val == -1) {
                int min = this.data[i + 1];
                int max = this.data[i + 2];
                i += 2;
                builder.append(min);
                builder.append("..");
                builder.append(max);
            } else {
                builder.append(val);
            }
            
            first = false;
        }
        return builder.toString();
    }
    
    @Override
    public String toString() {
        if (!hasDataFilter()) {
            return String.valueOf(getMaterial());
        } else {
            return String.valueOf(getMaterial()) + ":" + toDataRangeString();
        }
    }

}
