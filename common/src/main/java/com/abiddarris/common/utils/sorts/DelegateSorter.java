package com.abiddarris.common.utils.sorts;

public abstract class DelegateSorter<T> implements Sorter<T> {

    private Sorter<T> sorter;

    public Sorter<T> getSorter() {
        return this.sorter;
    }

    public void setSorter(Sorter<T> sorter) {
        this.sorter = sorter;
    }

    @Override
    public int compare(T o1, T o2) {
        return sorter.compare(o1, o2);
    }
}
