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

import static com.abiddarris.python3.PythonObject.findMethod;
import static com.abiddarris.python3.PythonObject.newClass;
import static com.abiddarris.python3.PythonObject.newDict;
import static com.abiddarris.python3.PythonObject.newFunction;
import static com.abiddarris.python3.PythonObject.newString;
import static com.abiddarris.python3.PythonObject.newTuple;

class Types {
    
    static PythonObject ModuleType;
    
    static void init() {
        ModuleType = newClass("module", newTuple(), newDict(
            newString("__init__"), newFunction(findMethod(Types.class, "moduleInit"), "self", "name")
        ));
    }
    
    static void mountModule(PythonObject modules) {
        PythonObject types = ModuleType.call(newString("types"));
        types.setAttribute("ModuleType", ModuleType);
        
        modules.setItem(newString("types"), types);
    }
    
    private static void moduleInit(PythonObject self, PythonObject name) {
        self.setAttribute("__name__", name);
    }
}
