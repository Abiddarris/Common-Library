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
package com.abiddarris.python3;

import static com.abiddarris.python3.Builtins.False;
import static com.abiddarris.python3.Builtins.True;
import static com.abiddarris.python3.Builtins.dict;
import static com.abiddarris.python3.Builtins.int0;
import static com.abiddarris.python3.Builtins.list;
import static com.abiddarris.python3.Builtins.str;
import static com.abiddarris.python3.Builtins.tuple;
import static com.abiddarris.python3.Builtins.type;
import static com.abiddarris.python3.Types.ModuleType;

import static java.util.Arrays.asList;

import com.abiddarris.python3.signature.PythonSignature;
import com.abiddarris.python3.signature.PythonSignatureBuilder;
import com.abiddarris.python3.trycatch.ExceptFinally;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Python {
    
    Python() {}
    
    public static PythonObject newString(String string) {
        PythonString object = new PythonString(string);
        object.setAttributeDirectly("__class__", str);
        
        return object;
    }
    
    public static PythonObject newInt(long value) {
        PythonObject object = new PythonInt(value);
        object.setAttributeDirectly("__class__", int0);

        return object;
    }
    
    public static PythonObject newBoolean(boolean val) { 
        return val ? True : False;
    }
    
    public static PythonObject newList(PythonObject... elements) {
        return newList(asList(elements));
    }
    
    public static PythonObject newList(List<PythonObject> elements) {
        return new PythonList(list, new ArrayList<>(elements));
    }
    
    public static ExceptFinally tryExcept(Runnable tryRunnable) {
        return new ExceptFinally(tryRunnable);
    }
    
    public static PythonObject newFunction(Class sourceClass, String methodName, PythonSignature signature) {
        return newFunction(findMethod(sourceClass, methodName), signature);
    }
    
    public static PythonObject newFunction(Class sourceClass, String methodName, String... argumentNames) {
        return newFunction(findMethod(sourceClass, methodName), argumentNames);
    }
     
    public static PythonObject newFunction(Method javaMethod, String... argumentNames) {
        PythonSignatureBuilder builder = new PythonSignatureBuilder();
        for(String argumentName : argumentNames) {
        	builder.addParameter(argumentName);
        }
        return newFunction(javaMethod, builder.build());
    }
    
    public static PythonObject newFunction(Method javaMethod, PythonSignature signature) {
        return new PythonFunction(javaMethod, signature);
    }
    
    public static PythonObject newTuple(PythonObject... elements) {
        return new PythonTuple(tuple, elements);
    }
    
    public static PythonObject newDict(Map<PythonObject, PythonObject> map) {
        return new PythonDict(dict, map);
    }
    
    public static PythonObject newDict(PythonObject... objects) {
        if(objects.length % 2 != 0) {
            throw new IllegalArgumentException("Missing value for " + objects[objects.length - 1]);
        }
        
        Map<PythonObject, PythonObject> map = new LinkedHashMap<>();
        for(int i = 0; i < objects.length; i += 2) {
        	map.put(objects[i], objects[i + 1]);
        }
        
        return newDict(map);
    }

    public static PythonObject format(String format, PythonObject... args) {
        return newString(format).callAttribute("format", args);
    }
    
    public static PythonObject newClass(String name, PythonObject bases, PythonObject attributes) {
        return newClass(null, name, bases, attributes);
    }
    
    public static PythonObject newClass(PythonObject _type, String name, PythonObject bases, PythonObject attributes) {
        if(_type == null) {
            _type = type;
        }
        
        return _type.call(newString(name), bases, attributes);
    }
    
    public static PythonObject createPackage(String name) {
        return createPackage(newString(name));
    }
    
    public static PythonObject createPackage(PythonObject name) {
        PythonObject module = ModuleType.call(name);
        module.setAttribute("__path__", newList());
        module.setAttribute("__package__", name);
        
        return module;
    }
    
    public static PythonObject createModule(PythonObject name) {
        PythonObject mod = ModuleType.call(name);
        
        String jName = name.toString(); 
        int end = jName.lastIndexOf(".");
        
        mod.setAttribute("__package__", newString(end != -1 ? jName.substring(0, end) : ""));
        
        return mod;
    }
    
    public static PythonObject createModule(String name) {
        return createModule(newString(name));
    }
    
    static Method findMethod(Class source, String name) {
        Method[] methods = Stream.of(source.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toArray(Method[]::new);
        
        if(methods.length > 1) {
            throw new IllegalArgumentException(methods.length + " found");
        }
        
        if(methods.length == 0) {
            throw new IllegalArgumentException("Not found");
        }
        
        methods[0].setAccessible(true);
        return methods[0];
    }

}
