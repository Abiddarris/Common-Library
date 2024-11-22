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

import static com.abiddarris.common.renpy.internal.Builtins.TypeError;
import static com.abiddarris.common.renpy.internal.Builtins.builtins;
import static com.abiddarris.common.renpy.internal.Builtins.dict;
import static com.abiddarris.common.renpy.internal.PythonObject.newFunction;

import com.abiddarris.common.renpy.internal.attributes.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PythonDict extends PythonObject {
    
    private static PythonObject dict_iterator;

    static void init2() {
        dict.defineAttribute("__module__", newString("builtins"));
        dict.defineFunction("__init__", PythonDict::init0, "self", "*iterable");

        DictItems.define();

        dict.defineFunction("items", PythonDict::items, "self");
    }

    static void init() {
        dict_iterator = Bootstrap.newClass(Builtins.type, newTuple(newString("dict_iterator"), newTuple()), new BootstrapAttributeHolder());
        dict_iterator.setAttribute("__next__", newFunction(findMethod(DictIterator.class, "next"), "self"));
    }

    private boolean useJavaIterator;
    private Map<PythonObject, PythonObject> map;
    
    PythonDict(PythonObject cls, Map<PythonObject, PythonObject> map) {
        super(new BootstrapAttributeHolder(), cls);

        this.useJavaIterator = cls == dict;
        this.map = map;
    }

    @Override
    public Iterator<PythonObject> iterator() {
        return map.keySet().iterator();
    }

    public Map<PythonObject, PythonObject> getMap() {
        return map;
    }
    
    private static PythonObject new0(PythonObject cls, PythonObject iterable) {
        return new PythonDict(cls, new LinkedHashMap<>());
    }

    private static void init0(PythonObject self, PythonObject iterable) {
        PythonDict dict = (PythonDict)self;
        PythonObject[] unpacker = new PythonObject[2];
        int pos = 0;

        if (iterable.length() > 1) {
            TypeError.call().raise();
        }

        if (iterable.length() == 1) {
            iterable = iterable.getItem(0);
        }

        for (PythonObject $args : iterable) {
            for (PythonObject kv : $args) {
                if (pos == unpacker.length) {
                    TypeError.call().raise();
                }
                unpacker[pos++] = kv;
            }
            if (pos != 2) {
                TypeError.call().raise();
            }
            pos = 0;

            dict.map.put(unpacker[0], unpacker[1]);
        }
    }
        
    private static PythonObject iter(PythonDict self) {
        return new DictIterator(self.map.keySet().iterator());
    }
        
    private static PythonObject dictGetItem(PythonDict self, PythonObject key) {
        PythonObject value = self.map.get(key);
        
        if(value == null) {
            Builtins.KeyError.call(key).raise();
        }
        
        return value;
    }
    
    private static void setItem(PythonDict self, PythonObject key, PythonObject value) {
        self.map.put(key, value);
    }

    private static PythonObject get(PythonDict self, PythonObject key, PythonObject default0) {
        ObjectWrapper<PythonObject> result = new ObjectWrapper<>(default0);
        tryExcept(() -> result.setObject(self.getItem(key)))
                .onExcept((e) -> {}, Builtins.KeyError)
                .execute();

        return result.getObject();
    }
    
    private static PythonObject contains(PythonDict self, PythonObject value) {
        return newBoolean(self.map.containsKey(value));
    }
    
    private static PythonObject len(PythonDict self) {
        return newInt(self.map.size());
    }
    
    private static void update(PythonDict self, PythonObject dict) {
        for(PythonObject k : dict) {
        	self.map.put(k, dict.getItem(k));
        }
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

    private static PythonObject items(PythonObject self) {
        return builtins.callAttribute("dict_items", self);
    }
        
    private static class DictIterator extends PythonObject {
            
        private Iterator<PythonObject> iterator;
            
        private DictIterator(Iterator<PythonObject> iterator) {
            super(new BootstrapAttributeHolder());
            
            this.iterator = iterator;
                
            setAttributeDirectly("__class__", dict_iterator);
        }
            
        private static PythonObject next(DictIterator self) {
            if(self.iterator.hasNext()) {
                return self.iterator.next();
            }
                
            Builtins.StopIteration.callAttribute("__new__", new PythonArgument()
                .addPositionalArgument(Builtins.StopIteration))
                .raise();
            return null;
        }
            
    }

    private static class DictItems {

        private static void define() {
            ClassDefiner definer = builtins.defineClass("dict_items");
            definer.defineFunction("__init__", DictItems::init, "self", "dict");

            definer.define();
        }

        private static void init(PythonObject self, PythonObject dict) {
            self.setAttribute("__dict__", dict);
        }

    }
}
