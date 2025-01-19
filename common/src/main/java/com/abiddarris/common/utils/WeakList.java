package com.abiddarris.common.utils;

import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class WeakList<V> extends AbstractList<V> {

    private List<WeakReference<V>> list = new ArrayList<>();

    @Override
    public boolean add(V v) {
        return super.add(v);
    }

    @Override
    public V remove(int index) {
        return super.remove(index);
    }

    @Override
    public V set(int index, V element) {
        WeakReference<V> old = list.set(index, new WeakReference<>(element));
        return old == null ? null : old.get();
    }

    @Override
    public V get(int i) {
        return list.;
    }

    @Override
    public int size() {
        return list.size();
    }
}
