package com.abiddarris.common.utils.recycler;

public class Poolable implements IPoolable {

    private boolean freed;

    @Override
    public void onFreed() {
        freed = true;
    }

    @Override
    public boolean isFree() {
        return freed;
    }

    @Override
    public void onPooled() {
        freed = false;
    }

    @Override
    public void checkNotFreed() {
        if(freed) {
            throw new FreedException("This object is in the pool!");
        }
    }

}
