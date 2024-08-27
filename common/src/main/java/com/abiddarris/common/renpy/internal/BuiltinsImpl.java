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

import static com.abiddarris.common.renpy.internal.Bootstrap.isInstanceBootstrap;
import static com.abiddarris.common.renpy.internal.PythonObject.AttributeError;
import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.KeyError;
import static com.abiddarris.common.renpy.internal.PythonObject.ModuleNotFoundError;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.issubclass;
import static com.abiddarris.common.renpy.internal.PythonObject.newBoolean;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.tuple;
import static com.abiddarris.common.renpy.internal.PythonObject.type;
import static com.abiddarris.common.renpy.internal.Sys.sys;

import static java.util.regex.Pattern.quote;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

public class BuiltinsImpl {
    
    static PythonObject importAs(String name) {
        return importAsInternal(
            name.split(quote(".")));
    }
    
    private static PythonObject len(PythonObject obj) {
        return obj.callTypeAttribute("__len__");
    }
    
    private static PythonObject isSubclass(PythonObject cls, PythonObject base) {
        return base.callTypeAttribute("__subclasscheck__", cls);
    }
    
    private static PythonObject isInstance(PythonObject instance, PythonObject cls) {
        if (isInstanceBootstrap(cls, type).toBoolean()) {
            return isInstanceBootstrap(instance, cls);
        }
        if (!isInstanceBootstrap(cls, tuple).toBoolean()) {
            TypeError.call(newString("isinstance() arg 2 must be a type, a tuple of types, or a union")).raise();
        }
        
        for (PythonObject cls0 : cls) {
            if (isInstance(instance, cls0).toBoolean()) {
                return True;
            }
        }
        
        return False;
    }
    
    private static PythonObject importImpl(PythonObject name) {
        String[] parts = name.toString().split(quote("."));
        importAsInternal(parts);
        
        return sys.getAttribute("modules").getItem(newString(parts[0]));
    }
    
    private static PythonObject importAsInternal(String[] parts) {
        ObjectWrapper<PythonObject> mod = new ObjectWrapper<>();
        ObjectWrapper<PythonObject> name = new ObjectWrapper(newString(parts[0]));
        
        tryExcept(() -> mod.setObject(
            sys.getAttribute("modules")
                .getItem(name.getObject()))).
        onExcept((e) -> {
            mod.setObject(importFromMetaPath(name.getObject()));
        }, KeyError).execute();
        
        for (int i = 1; i < parts.length; i++) {
            PythonObject submoduleName = newString(name.getObject() + "." + parts[i]);
            
            tryExcept(() -> mod.getObject().getAttribute("__path__")).
            onExcept((e) -> {
                ModuleNotFoundError.call(
                    newString(String.format("No module named %s; %s is not a package", name, submoduleName))
                ).raise();
            }, AttributeError).execute();
            
            name.setObject(submoduleName);
            int index = i;
            tryExcept(() -> mod.setObject(
                sys.getAttribute("modules")
                    .getItem(name.getObject()))).
            onExcept((e) -> {
                PythonObject submodule = importFromMetaPath(name.getObject());
                mod.getObject().setAttribute(parts[index], submodule);    
                mod.setObject(submodule);
            }, KeyError).execute();
        }
        
    	return mod.getObject();
    }
    
    private static PythonObject importFromMetaPath(PythonObject name) {
        for (PythonObject finder : sys.getAttribute("meta_path")) {
            PythonObject spec = finder.callAttribute("find_spec", name, None, None);
            
            if (!spec.toBoolean()) {
                continue;
            }
            
            spec.getAttribute("loader")
                .callAttribute("load_module", name);
            
            PythonObject mod = sys.getAttribute("modules").getItem(name);
            mod.setAttribute("__spec__", spec);
            
            return mod;
        }
        
        ModuleNotFoundError.call(
            newString(String.format("No module named %s", name))
        ).raise();
        
        return null;
    }
    
    private static PythonObject boolNew(PythonObject cls, PythonObject obj) {
        ObjectWrapper<PythonObject> returnValue = new ObjectWrapper<>();
        tryExcept(() -> {
            returnValue.setObject(obj.callTypeAttribute("__bool__"));
        }).onExcept((e) -> {
            tryExcept(() -> {
                returnValue.setObject(newBoolean(
                                obj.callTypeAttribute("__len__")
                                   .toInt() != 0));
            }).onExcept((e1) -> {
                returnValue.setObject(True);
            }, AttributeError).execute();
        }, AttributeError).execute();
        
        return returnValue.getObject();
    }
    
    private static void boolInit(PythonObject cls, PythonObject obj) {
    }
    
    private static PythonObject typeNew(PythonObject cls, PythonObject args) {
        if(args.length() == 1)  {
            return args.getItem(newInt(0))
                .getAttribute("__class__");
        }
        
        if(args.length() != 3) {
            TypeError.call().raise();
        }
        
        PythonObject attributes = args.getItem(newInt(2));
        PythonObject self = Bootstrap.newClass(cls, args);
        
        attributes.iterator().forEachRemaining(k -> {
            String key = k.toString();
            if(key.equals("__name__") || key.equals("__bases__") || key.equals("__class__")) {
                return;
            }
            self.attributes.put(key, attributes.getItem(k));
        });
        
        return self;
    }
    
    private static void typeInit(PythonObject self, PythonObject args) {
    }
    
    private static PythonObject typeStr(PythonObject self) {
        return newString(new StringBuilder()
            .append("<class '")
            .append(self.getAttribute("__name__").toString())
            .append("'>")
            .toString());
    }
    
    private static PythonObject objectStr(PythonObject self) {
        return newString(new StringBuilder()
            .append("<")
            .append(self.getTypeAttribute("__name__").toString())
            .append(" object>")
            .toString());
    }
    
    private static PythonObject objectInstanceCheck(PythonObject self, PythonObject other) {
        return issubclass.call(type.call(other), self);
    }
    
    private static PythonObject strNew(PythonObject cls, PythonObject obj) {
        return obj.callTypeAttribute("__str__");
    }
    
    private static void strInit(PythonObject cls, PythonObject obj) {
    }
    
    private static PythonObject typeSubclassCheck(PythonObject self, PythonObject other) {
        if (self == other) {
            return True;
        }
        
        return hasParent(other, self);
    }
    
    private static PythonObject hasParent(PythonObject target, PythonObject parent) {
        PythonObject parents = target.getAttribute("__bases__");
        int len = parents.length();
        for(int i = 0; i < len; ++i) {
        	PythonObject parent0 = parents.getItem(newInt(i));
            if(parent0 == parent) {
                return True;
            }
            
            PythonObject result = hasParent(parent0, parent);
            if(result.toBoolean()) {
                return True;
            }
        }
        
        return False;
    }
    
    private static PythonObject noneTypeNew(PythonObject cls) {
        return None;
    }
    
    private static void noneTypeInit(PythonObject self) {}
    
    private static void noneTypeCall(PythonObject self) {
        TypeError.call(newString("NoneType object is not callable")).raise();
    }
    
    private static PythonObject noneTypeBool(PythonObject self) {
        return False;
    }
}