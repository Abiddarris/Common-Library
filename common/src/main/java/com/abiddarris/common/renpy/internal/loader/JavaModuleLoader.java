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
package com.abiddarris.common.renpy.internal.loader;

import static com.abiddarris.common.reflect.Reflections.findMethodByName;
import static com.abiddarris.common.renpy.internal.Python.newClass;
import static com.abiddarris.common.renpy.internal.Python.newDict;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newFunction;
import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import com.abiddarris.common.renpy.internal.PythonObject;

import java.util.HashMap;
import java.util.Map;

public class JavaModuleLoader {
    
    private static boolean initialized;
    private static Map<String, ModuleLoader> loaders = new HashMap<>();
    
    private static PythonObject JavaModuleLoader0;
    private static PythonObject sys;
    private static PythonObject ModuleSpec;
    
    public static void init() {
        if(initialized) {
            return;
        }
        initialized = true;
        
        JavaModuleLoader0 = newClass("JavaModuleLoader", newTuple(), newDict(
            newString("find_spec"), newFunction(findMethodByName(JavaModuleLoader.class, "findSpec"), "name", "path", "target"),
            newString("load_module"), newFunction(findMethodByName(JavaModuleLoader.class, "loadModule"), "name")
        ));
       
        sys = JavaModuleLoader0.importModule("sys");
        ModuleSpec = JavaModuleLoader0.fromImport("importlib.machinery", "ModuleSpec")[0];
       
        sys.getAttribute("meta_path")
            .callAttribute("append", JavaModuleLoader0);
    }
    
    public static void registerLoader(String moduleName, ModuleLoader loader) {
        checkNonNull(moduleName, "module name cannot be null");
        checkNonNull(loader, "Loader cannot be null");
        
        loaders.put(moduleName, loader);
    }
    
    private static PythonObject findSpec(PythonObject name, PythonObject path, PythonObject target) {
        if(loaders.get(name.toString()) != null) {
            return ModuleSpec.call(name, JavaModuleLoader0);
        }
        return null;
    }
    
    private static PythonObject loadModule(PythonObject name) {
        String jName = name.toString();
        PythonObject module = loaders.get(jName).loadModule(jName);
        sys.getAttribute("modules")
            .setItem(name, module);
       
        return module;
    }
}
