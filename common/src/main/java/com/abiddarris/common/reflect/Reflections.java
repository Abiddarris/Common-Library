/***********************************************************************************
 * Copyright 2024 Abiddarris
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
package com.abiddarris.common.reflect;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * Utilities for reflections
 *
 * @since 1.0
 * @author Abiddarris
 */
public final class Reflections {
    
    public static Method findMethodByName(Class target, String name) {
        Method[] result = Stream.of(target.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toArray(Method[]::new);
       
        if(result.length > 1) {
            throw new MultipleMethodFoundException();
        }
        
        Method method;
        if(result.length == 1) {
            method = result[0];
        } else {
            Class superClass = target.getSuperclass();
            if(superClass == null) {
                return null;
            }
            method = findMethodByName(superClass, name);
        }
        
        return method;
    }
    
}
