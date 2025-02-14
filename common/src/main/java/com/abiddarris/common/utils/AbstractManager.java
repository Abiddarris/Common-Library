/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
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

import java.util.Map;
import java.util.HashMap;

public abstract class AbstractManager<K,V>{

    private Map<K,V> objs;

    public AbstractManager(){
        objs = new HashMap<>();
    }
    
    
    public V get(K key){
        V value = objs.get(key);
        if(value != null)
            return value;

        value = getInstance(key);
        if(value == null)
            throw new IllegalArgumentException("cannot return null on getInstance(K key)");
        objs.put(key,value);

        return value;
    }

    protected abstract V getInstance(K key);
}
