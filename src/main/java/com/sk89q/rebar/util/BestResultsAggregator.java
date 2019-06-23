/*
 * Rebar
 * Copyright (C) 2010-2014 Albert Pham <http://www.sk89q.com>
 */

package com.sk89q.rebar.util;

import java.util.ArrayList;
import java.util.List;

public class BestResultsAggregator<T> {
    
    public enum Order {
        WEIGHT_LOWER,
        WEIGHT_HIGHER,
    }
    
    private final List<Result> items = new ArrayList<Result>();
    private final int size;
    private final Order order;
    
    public BestResultsAggregator(int size, Order order) {
        this.size = size;
        this.order = order;
    }
    
    public boolean add(T item, int weight) {
        int index = 0;
        for (Result res : items) {
            if ((order == Order.WEIGHT_LOWER && weight < res.getWeight())
                    || (order == Order.WEIGHT_HIGHER && weight > res.getWeight())) {
                items.add(index, new Result(item, weight));
                if (items.size() > size) {
                    items.remove(items.size() - 1);
                }
                return true;
            }
            index++;
        }
        if (items.size() < size) {
            items.add(new Result(item, weight));
            return true;
        }
        
        return false;
    }
    
    public List<T> getResults() {
        List<T> results = new ArrayList<T>();
        for (Result res : items) {
            results.add(res.getItem());
        }
        return results;
    }
    
    private class Result {
        private T item;
        private int weight;
        
        public Result(T item, int weight) {
            this.item = item;
            this.weight = weight;
        }

        public T getItem() {
            return item;
        }

        public int getWeight() {
            return weight;
        }
    }
    

}
