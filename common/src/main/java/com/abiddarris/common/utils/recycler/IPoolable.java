package com.abiddarris.common.utils.recycler;

public interface IPoolable {

    void onFreed();

    boolean isFree();

    void onPooled();

    void checkNotFreed();

}
