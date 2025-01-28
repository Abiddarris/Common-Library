package com.abiddarris.python3.attributes;

public class DisableAttributeAccessOptimizationEvent implements Event {

    private Type type;

    public DisableAttributeAccessOptimizationEvent(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SETTER, GETTER
    }
}
