package com.abiddarris.common.utils.recycler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

public abstract class ObjectRecycler<K,V extends IPoolable> {

    private PolicyGroup globalPolicyGroup = new PolicyGroup(ReferencePolicy.SINGLE_REFERENCE, 
        SavePolicy.DEFAULT);
    private Map<K, KeyInfo> pools = new HashMap<>();
    private Set<V> activeObjects = new LinkedHashSet<>();
    private Map<V, ValueInfo> valueInfos = new HashMap<>();

    protected V get(K key) {
        KeyInfo info = getKeyInfo(key);

        synchronized(this) { 
            V candidate = findCandidate(info.values, key);

            if(candidate == null) {
                candidate = create(key);
                info.values.add(candidate);
                valueInfos.put(candidate, new ValueInfo());
            }         
            
            addToActiveCache(key, candidate);

            return candidate;
        }
    }

    public synchronized boolean free(V value) {       
        if(!activeObjects.contains(value)) return false;
        
        ValueInfo info = valueInfos.get(value);
        info.stackTrace = null;
        info.reference--;
        if(info.reference <= 0) {
            activeObjects.remove(value);
            value.onFreed();
        }        
        
        return true;
    }

    protected synchronized void registerToCache(K key, V value) {
        getKeyInfo(key).values
            .add(value);
        valueInfos.put(value, new ValueInfo());     
        addToActiveCache(key,value);    
    }

    public V[] getActiveObjects(V[] array) {
        return activeObjects.toArray(array);
    }

    public StackTraceElement[] getStackTrace(V value) {    
        ValueInfo info = valueInfos.get(value);
        
        return info == null ? null : info.stackTrace;
    }

    public synchronized void release() {
        pools.clear();
        activeObjects.clear();
        valueInfos.clear();       
    }

    public void addPolicies(K key, IPolicy... policies) {
        getKeyInfo(key)
            .group.addPolicies(policies);
    }
    
    public void addPolicies(IPolicy... policies) {
        globalPolicyGroup.addPolicies(policies);
    }
    
    public int getCacheSize() {
        int size = 0;
        for(KeyInfo info : pools.values()) {
            size += info.values.size();
        }
        return size;
    }
    
    public int getReference(V value) {
        ValueInfo info = valueInfos.get(value);
        return info == null ? null : info.reference;
    }

    private boolean isMultipleReference(K key) {
        return getKeyInfo(key)
            .group.findPolicy(ReferencePolicy.class) == ReferencePolicy.MULTIPLE_REFERENCE;
    }

    private synchronized KeyInfo getKeyInfo(K key) {
        KeyInfo info = pools.get(key);
        if(info == null) {
            info = new KeyInfo();
            pools.put(key, info);
        }
        return info;
    }

    private V findCandidate(List<V> cachedObjects, K key) {
        for (V value : cachedObjects) {
            V candidate = isMultipleReference(key) ?  value : (activeObjects.contains(value) ? null : value); 
            if (candidate != null) return candidate;
        }   
        return null;
    }

    private void addToActiveCache(K key, V value) {                   
        ValueInfo info = valueInfos.get(value);
        info.reference++;
        if(info.reference == 1) {
            activeObjects.add(value);
            value.onPooled();          
        }
        if(findPolicy(key, SavePolicy.class) == SavePolicy.DEFAULT || isMultipleReference(key)) {
            return;
        }

        Throwable throwable = new Throwable();
        StackTraceElement[] stackTrace = throwable.getStackTrace();       
         info.stackTrace = Arrays.copyOfRange(stackTrace, 2, stackTrace.length);
    }
    
    private IPolicy findPolicy(K key, Class clazz) {
        return getKeyInfo(key).group
            .findPolicy(clazz);
    }
   
    protected abstract V create(K key);

    private class KeyInfo {
        private List<V> values = new ArrayList<>();          
        private PolicyGroup group = new PolicyGroup(globalPolicyGroup);       
    }
    
    private class ValueInfo {       
        private int reference;
        private StackTraceElement[] stackTrace;
    }
}
