/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.Random;

public class WeightedProbability<T> {
    
    private final Random random = new Random();
    private T[] items;
    private int[] weights;
    private int totalWeight;
    
    public WeightedProbability(T[] items, int[] weights) {
        if (items.length != weights.length) {
            throw new IllegalArgumentException("Length of items array does not match length of weights array");
        }
        this.items = items;
        this.weights = weights;
        totalWeight = 0;
        for (int weight : weights) {
            totalWeight += weight;
        }
    }
    
    public T next() {
        int rand = random.nextInt(totalWeight);
        int weight = 0;
        for (int i = 0; i < weights.length; i++) {
            weight += weights[i];
            if (rand < weight) {
                return items[i];
            }
        }
        
        // Should not happen
        throw new IllegalArgumentException("next() failed, got null");
    }
    
}
