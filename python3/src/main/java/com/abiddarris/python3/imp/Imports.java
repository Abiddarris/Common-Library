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
package com.abiddarris.python3.imp;

import static com.abiddarris.python3.PythonObject.*;
import static com.abiddarris.python3.Sys.sys;

import static java.lang.System.arraycopy;
import static java.util.regex.Pattern.quote;

import com.abiddarris.python3.Builtins;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.common.utils.ObjectWrapper;
import com.abiddarris.python3.core.Functions;

import java.util.ArrayList;
import java.util.List;

public class Imports {

    private static final PythonObject ModuleType = importFrom("types", "ModuleType")[0];

    public static PythonObject[] importFrom(String modName, String attributeName, String... attributeNames) {
        return importFrom(modName, NullLoadTarget.INSTANCE, attributeName, attributeNames);
    }
    
    public static PythonObject[] importFrom(String modName, LoadTarget target, String attributeName, String... attributeNames) {
        PythonObject mod = importAs(modName);
        
        String[] attributeNames0 = new String[attributeNames.length + 1];
        attributeNames0[0] = attributeName;
        
        arraycopy(attributeNames, 0, attributeNames0, 1, attributeNames.length);
        
        List<PythonObject> attributes = new ArrayList<>();
        ObjectWrapper<PythonObject> attribute = new ObjectWrapper<>();
        for(String attributeName0 : attributeNames0) {
            attribute.setObject(null);

            tryExcept(() -> attribute.setObject(mod.getAttribute(attributeName0))).
                    onExcept((e) -> {}, Builtins.AttributeError).
                    execute();

            if (attribute.getObject() == null && Functions.isInstance(mod, ModuleType).toBoolean() && Builtins.hasattr.call(mod, newString("__path__")).toBoolean()) {
                tryExcept(() -> attribute.setObject(importAs(modName + "." + attributeName0))).
                        onExcept((e) -> {}, Builtins.ModuleNotFoundError).
                        execute();
            }

            if (attribute.getObject() == null) {
                Builtins.ImportError.call(newString(
                        String.format("cannot import name '%s' from '%s'", attributeName0, modName)
                )).raise();
            }

            target.onImport(attributeName0, attribute.getObject());
            
            attributes.add(attribute.getObject());
        }
        
        return attributes.toArray(PythonObject[]::new);
    }
    
    public static PythonObject importModule(String name) {
    	String[] parts = name.split(quote("."));
        importAs(name);
        
        return sys.getAttribute("modules").getItem(newString(parts[0]));
    }
    
    public static PythonObject importAs(String name) {
        return importAsInternal(
            name.split(quote(".")));
    }
    
    private static PythonObject importAsInternal(String[] parts) {
        ObjectWrapper<PythonObject> mod = new ObjectWrapper<>();
        ObjectWrapper<PythonObject> name = new ObjectWrapper<>(newString(parts[0]));
        
        tryExcept(() -> mod.setObject(
            sys.getAttribute("modules")
                .getItem(name.getObject()))).
        onExcept((e) -> {
            mod.setObject(importFromMetaPath(name.getObject()));
        }, Builtins.KeyError).execute();
        
        for (int i = 1; i < parts.length; i++) {
            PythonObject submoduleName = newString(name.getObject() + "." + parts[i]);
            
            tryExcept(() -> mod.getObject().getAttribute("__path__")).
            onExcept((e) -> {
                Builtins.ModuleNotFoundError.call(
                    newString(String.format("No module named %s; %s is not a package", name, submoduleName))
                ).raise();
            }, Builtins.AttributeError).execute();
            
            name.setObject(submoduleName);
            int index = i;
            tryExcept(() -> mod.setObject(
                sys.getAttribute("modules")
                    .getItem(name.getObject()))).
            onExcept((e) -> {
                PythonObject submodule = importFromMetaPath(name.getObject());
                mod.getObject().setAttribute(parts[index], submodule);    
                mod.setObject(submodule);
            }, Builtins.KeyError).execute();
        }
        
    	return mod.getObject();
    }
    
    private static PythonObject importFromMetaPath(PythonObject name) {
        for (PythonObject finder : sys.getAttribute("meta_path")) {
            PythonObject spec = finder.callAttribute("find_spec", name, Builtins.None, Builtins.None);
            
            if (!spec.toBoolean()) {
                continue;
            }
            
            spec.getAttribute("loader")
                .callAttribute("load_module", name);
            
            PythonObject mod = sys.getAttribute("modules").getItem(name);
            mod.setAttribute("__spec__", spec);
            
            return mod;
        }
        
        Builtins.ModuleNotFoundError.call(
            newString(String.format("No module named %s", name))
        ).raise();
        
        return null;
    }
}
