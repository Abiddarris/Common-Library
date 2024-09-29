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
package com.abiddarris.common.renpy.internal.builder;

import static com.abiddarris.common.renpy.internal.Python.newClass;
import static com.abiddarris.common.renpy.internal.Python.newFunction;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.Python.newDict;

import com.abiddarris.common.renpy.internal.PythonObject;

import com.abiddarris.common.renpy.internal.defineable.Defineable;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassDefiner implements Defineable {
    
    private Map<String, PythonObject> attributes = new LinkedHashMap<>();
    private String className;
    private PythonObject[] bases;
    private PythonObject moduleName;
    private Target target;
    
    public ClassDefiner(String className, PythonObject[] bases, PythonObject moduleName, Target target) {
        this.className = className;
        this.bases = bases;
        this.moduleName = moduleName;
        this.target = target;
        
        attributes.put("__module__", moduleName);
    }

    @Override
    public PythonObject getModuleName() {
        return moduleName;
    }

    public PythonObject defineAttribute(String name, PythonObject attribute) {
        attributes.put(name, attribute);
        
        return attribute;
    }
    
    public PythonObject defineFunction(String name, Class source, String methodName, String... parameters) {
        return initFunction(name, newFunction(source, methodName, parameters));
    }

    public PythonObject defineFunction(String name, PythonObject decorator, Class source, String methodName, String... parameters) {
        return initFunction(name,
                decorator.call(newFunction(source, methodName, parameters)));
    }

    public PythonObject defineFunction(String name, PythonObject decorator, Class source, String methodName, PythonSignature signature) {
        return initFunction(name,
                decorator.call(newFunction(source, methodName, signature)));
    }

    public PythonObject defineFunction(String name, Class source, String methodName, PythonSignature signature) {
        return initFunction(name, newFunction(source, methodName, signature));
    }
    
    public ClassDefiner defineClass(String name, PythonObject... bases) {
        return new ClassDefiner(name, bases, moduleName, new ClassDefinerTarget(this));
    }
    
    public PythonObject define() {
        PythonObject[] attributes = new PythonObject[this.attributes.size() * 2];
        int index = 0;
        for(String key : this.attributes.keySet()) {
        	attributes[index++] = newString(key);
            attributes[index++] = this.attributes.get(key);
        }
        
        PythonObject clazz = newClass(className, newTuple(bases), newDict(attributes));
        target.onDefine(className, clazz);
        
        return clazz;
    }
    
    private PythonObject initFunction(String name, PythonObject function) {
        function.setAttribute("__module__", moduleName);
        
        attributes.put(name, function);
        
        return function;
    }
}
