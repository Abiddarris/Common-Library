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
package com.abiddarris.python3.core;

import static com.abiddarris.python3.core.Errors.raiseAttributeError;

import com.abiddarris.python3.Builtins;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.attributes.AttributeManager;

public class Super {
    
    private static boolean init;
    
    public static PythonObject define(PythonObject builtins) {
        if (init) {
            return Builtins.super0;
        }
        init = true;
        
        ClassDefiner definer = builtins.defineClass("super");
        definer.defineFunction("__init__", Super.class, "init", "self", "cls", "instance");
        definer.defineFunction("__getattribute__", Super.class, "getAttribute", "self", "name");
        
        return definer.define();
    }
    
    private static void init(PythonObject self, PythonObject cls, PythonObject instance) {
        PythonObject instanceType = Types.type(instance);
        
        self.setAttribute("__thisclass__", cls);
        self.setAttribute("__self__", instance);
        self.setAttribute("__self_class__", instanceType);
    }
    
    private static PythonObject getAttribute(PythonObject self, PythonObject name) {
        AttributeManager attributeManager = self.getAttributes();
        
        PythonObject searchStart = attributeManager.findAttribute("__thisclass__");
        PythonObject selfClass = attributeManager.findAttribute("__self_class__");
        PythonObject instance = attributeManager.findAttribute("__self__");
        
        PythonObject attribute = instance.getAttributes().searchAttribute(searchStart, selfClass, name.toString());
        if (attribute == null) {
            raiseAttributeError(self, name);
        }
        
        return attribute;
    }
}
