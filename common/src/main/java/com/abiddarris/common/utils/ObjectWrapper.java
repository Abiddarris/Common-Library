package com.abiddarris.common.utils;

/**
 * Class that wraps an object
 */
public class ObjectWrapper<T> {
    
    private T object;
    
    public ObjectWrapper(T object) {
        this.object = object;
    }

    public T getObject() {
        return this.object;
    }
    
    public void setObject(T object) {
        this.object = object;
    }
}
