package com.caucraft.mciguiv3.util;

import java.util.TreeMap;

/**
 *
 * @author caucow
 */
public class WeightedRandom<T> {
    
    private TreeMap<Double, T> weightMap;
    private double total;
    
    public WeightedRandom() {
        weightMap = new TreeMap<>();
    }
    
    public void put(double weight, T x) {
        weightMap.put(total, x);
        total += weight;
    }
    
    public T get() {
        return get(total * Math.random());
    }
    
    public double getTotal() {
        return total;
    }
    
    public int getSize() {
        return weightMap.size();
    }
    
    public T get(double calculatedRandom) {
        T t = weightMap.get(calculatedRandom);
        if (t == null) {
            return weightMap.lowerEntry(calculatedRandom).getValue();
        }
        return t;
    }
    
}
