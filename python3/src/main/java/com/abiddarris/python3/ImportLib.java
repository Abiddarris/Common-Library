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

import static com.abiddarris.python3.Python.createPackage;
import static com.abiddarris.python3.Python.findMethod;
import static com.abiddarris.python3.Python.newClass;
import static com.abiddarris.python3.Python.newDict;
import static com.abiddarris.python3.Python.newFunction;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.Sys.sys;
import static com.abiddarris.python3.Types.ModuleType;

class ImportLib {
    
    private static PythonObject importlib;
    private static PythonObject machinery;
    
    static void init() {
        machinery = ModuleType.call(newString("importlib.machinery"));
        machinery.setAttribute("__package__", newString("importlib"));
        machinery.setAttribute("ModuleSpec", newClass("ModuleSpec", newTuple(), newDict(
            newString("__init__"), newFunction(findMethod(ImportLib.class, "moduleSpecInit"), "self", "name", "loader")
        )));
        
        importlib = createPackage("importlib");
        importlib.setAttribute("machinery", machinery);
        
        PythonObject modules = sys.getAttribute("modules");
        modules.setItem(newString("importlib"), importlib);
        modules.setItem(newString("importlib.machinery"), machinery);
    }
    
    private static void moduleSpecInit(PythonObject self, PythonObject name, PythonObject loader) {
        self.setAttribute("name", name);
        self.setAttribute("loader", loader);
    }
}
