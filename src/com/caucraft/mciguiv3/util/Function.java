package com.caucraft.mciguiv3.util;

/**
 *
 * @author caucow
 */
public interface Function<T> {
    
    public T doWork(Object... args) throws Exception;
    
}
