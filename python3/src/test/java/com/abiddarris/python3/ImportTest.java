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

import static com.abiddarris.common.reflect.Reflections.findMethodByName;
import static com.abiddarris.python3.Python.createModule;
import static com.abiddarris.python3.Python.createPackage;
import static com.abiddarris.python3.Python.newClass;
import static com.abiddarris.python3.Python.newDict;
import static com.abiddarris.python3.Python.newFunction;
import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.Builtins.None;
import static com.abiddarris.python3.Builtins.True;
import static com.abiddarris.python3.Builtins.__import__;
import static com.abiddarris.python3.PythonObject.newString;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.python3.loader.JavaModuleLoader;

import org.junit.jupiter.api.Test;

public class ImportTest {
    
    private static PythonObject sys;
    private static PythonObject Loader;
    
    @Test
    public void importTest() {
        __import__.call(newString("sys"));
    }
    
    @Test
    public void customLoader() {
        sys = __import__.call(newString("sys"));
        
        CustomLoaderImpl.ModuleSpec = sys.getAttribute("modules").getItem(newString("importlib.machinery"))
            .getAttribute("ModuleSpec");
        
        Loader = newClass("Loader", newTuple(), newDict(
            newString("find_spec"), newFunction(
                    findMethodByName(CustomLoaderImpl.class, "findSpec"),
                    "name", "path", "target"),
            newString("load_module"), newFunction(
                    findMethodByName(CustomLoaderImpl.class, "loadModule"),
                    "name")
        ));
        sys.getAttribute("meta_path")
            .callAttribute("insert", newInt(0), Loader);
        
        PythonObject engine = __import__.call(newString("engine.gl20"));
        
        assertEquals("gl20", engine.getAttribute("renderer_name").toString());
        assertEquals("Android", engine.getAttribute("gl20")
                .getAttribute("platform").toString());
    }
    
    @Test
    public void importOnObject_root() {
        PythonObject sys = __import__.call(newString("sys"));
        PythonObject simpleModule = createModule("simplemodule");
        
        assertEquals(sys, simpleModule.importModule("sys"));
        assertEquals(sys, simpleModule.getAttribute("sys"));
    }
      
    @Test
    public void fromImportOnObject() {
        PythonObject modules = __import__.call(newString("sys")).getAttribute("modules");
        PythonObject simpleModule = createModule("simplemodule");
        
        assertEquals(modules, simpleModule.fromImport("sys", "modules")[0]);
        assertEquals(modules, simpleModule.getAttribute("modules"));
    }
    
    @Test
    public void javaModuleLoader() {
        JavaModuleLoader.registerLoader("cio", (name) -> {
            PythonObject cio = createModule("cio");
            cio.setAttribute("fast_io_supported", True);
                
            return cio;    
        });
        
        PythonObject cio = __import__.call(newString("cio"));
        assertEquals(True, cio.getAttribute("fast_io_supported"));
    }
    
    public static class CustomLoaderImpl {
        
        private static PythonObject ModuleSpec;
        
        public static PythonObject findSpec(PythonObject name, PythonObject path, PythonObject target) {
            String jName = name.toString();
            if(jName.equals("engine") || jName.equals("engine.gl20")) {
                return ModuleSpec.call(name, Loader);
            }
            
            return None;
        }
        
        public static PythonObject loadModule(PythonObject name) {
            String jname = name.toString();
            PythonObject module = null;
            if(jname.equals("engine")) {
                module = createPackage(name);
                module.setAttribute("renderer_name", newString("gl20"));
            } else if(jname.equals("engine.gl20")) {
                module = createModule(name);
                module.setAttribute("platform", newString("Android"));
            }
            
            if (module != null) {
                sys.getAttribute("modules")
                    .setItem(name, module);
            }
            
            return null;
        }
        
    }
}
