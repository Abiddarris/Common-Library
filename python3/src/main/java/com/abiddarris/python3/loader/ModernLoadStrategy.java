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
package com.abiddarris.python3.loader;

import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.loader.JavaModuleLoader.sys;

import com.abiddarris.python3.PythonObject;

class ModernLoadStrategy implements LoadStrategy {

    private String name;
    private ModuleFactory factory;
    private ModuleExecutor executor;

    ModernLoadStrategy(String name, ModuleFactory factory, ModuleExecutor executor) {
        this.name = name;
        this.factory = factory;
        this.executor = executor;
    }

    @Override
    public PythonObject loadModule() {
        PythonObject module = factory.createModule(name);
        
        sys.getAttribute("modules")
            .setItem(newString(name), module);
        
        executor.executeModule(module);
        
        return module;
    }
}
