/***********************************************************************************
 * Copyright 2024 - 2025 Abiddarris
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************************/
package com.abiddarris.common.utils;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Provides support for making {@code value} to reference strongly or
 * weakly.
 *
 * <p>This class does not support {@code null} or {@code WeakReference} for {@code value}.
 * Any attempt to do that will result UnsupportedOperationException if attempting to past 
 * {@code WeakReference} and {@code NullPointerException} if attempting to past {@code null}.
 *
 * If {@code value} is weak reference and it was collected by garbage collector any call to 
 * {@link #get(K)} that pass the {@code key} of collected {@code value} will automaticly remove
 * the {@code key} from the map.
 *
 * @author Abiddarris
 */
public class WeakableValueMap<K,V> {
    
    /**
     * Clean executor
     */
    private static final ScheduledExecutorService CLEAN_EXECUTOR = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    
    /**
     * Map to store values
     */
    private Map<K, Object> values = new HashMap<>();  
    
    /**
     * Runnable that responsible for calling {@link #cleanUnusedKey()}
     */
    private AutomaticCleanupRunnable cleanupRunnable;
    
    /**
     * Create new {@code WeakableValueMap}
     */
    public WeakableValueMap() {
        enableAutomaticKeyCleanup();
    }
    
    /**
     * Returns {@code value} from specified {@code key}
     *
     * @param key Key
     * @return {@code null} if key does not exist in this map, otherwise
     *         return non-{@code null}
     */
    public V get(K key) {
        Object value = values.get(key);
        if(value == null) {
            return null;
        }
            
        if(!(value instanceof WeakReference)) {
            return (V)value;
        }              

        WeakReference<V> reference = (WeakReference<V>)value;
        V obj = reference.get();
        if(obj == null) {
            values.remove(key);
        }
        return obj;
    }
    
    /**
     * Put {@code key} and {@code value} to the map as weak reference
     *
     * @param key Key
     * @param value Value must not be {@code null} or instance of {@code WeakReference}
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws UnsupportedOperationException if {@code value} is instance of {@code WeakReference}
     */
    public void put(K key, V value) {
        validateValue(value);
        
        values.put(key,new WeakReference<V>(value));     
    }

    /**
     * Put {@code key} and {@code value} to the map as strong reference
     *
     * @param key Key
     * @param value Value must not be {@code null} or instance of {@code WeakReference}
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws UnsupportedOperationException if {@code value} is instance of {@code WeakReference}
     */   
    public void putStrong(K key, V value) {
        validateValue(value);      
        
        values.put(key, value);    
    }

    /**
     * Change {@code value} to weak reference from specified key
     *
     * @param key Key
     * @return {@code false} if the {@code value} of the {@code key} is 
     *         already weak reference, Otherwise returns {@code true}
     */
    public boolean toWeakReference(K key) {
        if(isKeyValueWeak(key)) return false;
       
        put(key, (V) values.get(key));
        return true;
    }

    /**
     * Change {@code value} to strong reference from specified key
     *
     * @param key Key
     * @return {@code false} if the {@code value} of the {@code key} is 
     *         already strong reference, Otherwise returns {@code true}
     */
    public boolean toStrongReference(K key) {
        if(isKeyValueStrong(key)) return false;
        
        WeakReference<V> reference = (WeakReference<V>) values.get(key);
        V value = reference.get();
        if(value == null) return false;
        
        putStrong(key, value);
        return true;
    }  
    
    /**
     * Returns {@code true} if {@code value} from specified key is weak reference
     * 
     * @param key Key
     * @return {@code true} if {@code value} from specified key is weak reference.
     *         Otherwise returns {@code false}
     */
    public boolean isKeyValueWeak(K key) {
        return values.get(key) instanceof WeakReference;
    }
    
    /**
     * Returns {@code true} if {@code value} from specified key is strong reference
     *
     * @param key Key
     * @return {@code true} if {@code value} from specified key is strong reference.
     *         Otherwise returns {@code false}
     */
    public boolean isKeyValueStrong(K key) {
        return !isKeyValueWeak(key);
    }
    
    /**
     * Clean {@code WeakReference} whose its {@code get()} method returns {@code null}
     */
    public void cleanUnusedKey() {
        List<K> keys = null;
        for(K key : values.keySet()) {
            Object v = values.get(key);
            
            if(!(v instanceof WeakReference)) {
                continue;             
            }
            
            WeakReference<V> ref = (WeakReference<V>)v;
            if(ref.get() != null) {
                continue;
            }
            
            if(keys == null) keys = new LinkedList<>();
            keys.add(key);          
        }
        if(keys == null) return;
        
        for(K key : keys) {          
            values.remove(key);
        }
    }
    
    /**
     * Enable automatic call of {@link #cleanUnusedKey()} every 1 minute
     *
     * <p>This method automaticly called when creating this object
     *
     * @return {@code false} if this feature already enabled. otherwise return {@code true}
     */
    public boolean enableAutomaticKeyCleanup() {
        if(isAutomaticKeyCleanup()) return false;
        
        cleanupRunnable = new AutomaticCleanupRunnable(this);
        cleanupRunnable.setFuture(CLEAN_EXECUTOR.scheduleWithFixedDelay(cleanupRunnable, 0, 1, TimeUnit.MINUTES));    
        
        return true;
    }
    
    /**
     * Disable automatic call of {@link #cleanUnusedKey()} 
     *
     * @return {@code false} if this feature already disabled. otherwise return {@code true}
     */
    public boolean disableAutomaticKeyCleanup() {
        if(!isAutomaticKeyCleanup()) return false;
        
        cleanupRunnable.shutdown();
        cleanupRunnable = null;
        
        return true;
    }
    
    /**
     * Returns {@code true} if {@link #enableAutomaticKeyCleanup()} is enabled
     *
     * @return {@code true} if {@link #enableAutomaticKeyCleanup()} is enabled.
     *         Otherwise returns {@code false}
     */
    public boolean isAutomaticKeyCleanup() {
        return cleanupRunnable != null;
    }
    
    /**
     * Validate {@code value}
     *
     * @param value Value to validate
     * @throws NullPointerException if {@code value} is {@code null}
     * @throws UnsupportedOperationException if {@code value} is instance of {@code WeakReference}
     */
    private void validateValue(V value) throws UnsupportedOperationException {
        Preconditions.checkNonNull(value, "");

        if (value instanceof WeakReference)
            throw new UnsupportedOperationException("Can not pass value that instance of WeakReference");
    }
    
    /**
     * Class that responsible for calling {@link #cleanUnusedKey()} automaticly
     */
    private static class AutomaticCleanupRunnable implements Runnable {
        
        /**
         * Hold {@code Future} that returned by {@code Executor}
         */
        private Future<?> future;
        
        /**
         * Hold {@code WeakableValueMap} that will be called by this class
         */
        private WeakReference<WeakableValueMap> map;
        
        /**
         * Create new {@code AutomaticCleanupRunnable}
         *
         * @param map {@code WeakableValueMap} that will be called by this class
         */
        private AutomaticCleanupRunnable(WeakableValueMap map) {
            this.map = new WeakReference<WeakableValueMap>(map);
        }

        /**
         * Set the {@code Future} that returned by {@code Executor}
         */
        private void setFuture(Future<?> future) {
            this.future = future;
        }
        
        /**
         * Remove this {@code Runnable} from the {@code Executor}
         */
        private void shutdown() {
            future.cancel(true);
        }      

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {          
            WeakableValueMap map = this.map.get();
            if(map == null) {
                shutdown();
                return;
            }
            
            map.cleanUnusedKey();
        }      
        
    }
}
