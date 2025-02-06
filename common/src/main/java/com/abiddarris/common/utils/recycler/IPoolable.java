package com.gretta.util.recycler;

public interface IPoolable {

    void onFreed();

    boolean isFree();

    void onPooled();

    void checkNotFreed();

}
