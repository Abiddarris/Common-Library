package com.abiddarris.common.utils.sorts;

public class DescendingSorter<T> extends DelegateSorter<T> {

    @Override
    public int compare(T first, T second) {
        return getSorter().compare(second, first);
    }
}
