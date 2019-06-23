/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeightedPriorityList<T> {
    
    private final Random random = new Random();
    private List<T> items;
    private List<Integer> weights;
    private int totalWeight;
    
    public WeightedPriorityList(List<T> items, List<Integer> weights) {
        if (items.size() != weights.size()) {
            throw new IllegalArgumentException("Length of items array does not match length of weights array");
        }
        this.items = items;
        this.weights = weights;
        totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
    }
    
    public WeightedPriorityList() {
        this.items = new ArrayList<T>();
        this.weights = new ArrayList<Integer>();
        totalWeight = 0;
    }
    
    public void add(T item, int weight) {
        items.add(item);
        weights.add(weight);
        totalWeight += weight;
    }
    
    public T next() {
        int rand = random.nextInt(totalWeight);
        int weight = 0;
        for (int i = 0; i < weights.size(); i++) {
            weight += weights.get(i);
            if (rand < weight) {
                return items.get(i);
            }
        }
        
        // Should not happen
        throw new IllegalArgumentException("next() failed, got null");
    }
    
}
