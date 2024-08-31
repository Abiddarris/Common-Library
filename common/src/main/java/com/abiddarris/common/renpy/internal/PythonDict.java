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
package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonObject.findMethod;
import static com.abiddarris.common.renpy.internal.PythonObject.newFunction;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.type;

import com.abiddarris.common.renpy.internal.model.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;

import java.util.Iterator;
import java.util.Map;

class PythonDict extends PythonObject {
    
    private static PythonObject dict_iterator;
        
    static void init() {
        dict_iterator = Bootstrap.newClass(type, newTuple(newString("dict_iterator"), newTuple()));
        dict_iterator.setAttribute("__next__", newFunction(findMethod(DictIterator.class, "next"), "self"));
    }
        
    private Map<PythonObject, PythonObject> map;
    
    PythonDict(Map<PythonObject, PythonObject> map) {
        super(new BootstrapAttributeHolder());
        
        this.map = map;
    }
        
    private static PythonObject iter(PythonDict self) {
        return new DictIterator(self.map.keySet().iterator());
    }
        
    private static PythonObject dictGetItem(PythonDict self, PythonObject key) {
        PythonObject value = self.map.get(key);
               
        if(value == null) {
            KeyError.call(key).raise();
        }
        
        return value;
    }
    
    private static void setItem(PythonDict self, PythonObject key, PythonObject value) {
        self.map.put(key, value);
    }
    
    private static PythonObject contains(PythonDict self, PythonObject value) {
        return newBoolean(self.map.containsKey(value));
    }
    
    private static PythonObject len(PythonDict self) {
        return newInt(self.map.size());
    }
    
    private static PythonObject str(PythonDict self) {
        StringBuilder builder = new StringBuilder("{");
        self.map.forEach((k, v) -> builder.append(k)
            .append(": ")
            .append(v)
            .append(", "));
        
        if (self.map.size() > 1) {
            builder.delete(builder.length() - 2, builder.length());
        }
        
        builder.append("}");
        
        return newString(builder.toString());
    }
        
    private static class DictIterator extends PythonObject {
            
        private Iterator<PythonObject> iterator;
            
        private DictIterator(Iterator<PythonObject> iterator) {
            super(new BootstrapAttributeHolder());
            
            this.iterator = iterator;
                
            setAttribute("__class__", dict_iterator);
        }
            
        private static PythonObject next(DictIterator self) {
            if(self.iterator.hasNext()) {
                return self.iterator.next();
            }
                
            StopIteration.callAttribute("__new__", new PythonArgument()
                .addPositionalArgument(StopIteration))
                .raise();
            return null;
        }
            
    }
}
