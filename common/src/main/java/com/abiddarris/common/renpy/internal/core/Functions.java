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
package com.abiddarris.common.renpy.internal.core;

import static com.abiddarris.common.renpy.internal.PythonObject.*;

import com.abiddarris.common.renpy.internal.PythonObject;

public class Functions {
    
    private static PythonObject isInstanceBootstrap(PythonObject instance, PythonObject cls) {
        return cls.callTypeAttribute("__instancecheck__", instance);
    }
    
    public static PythonObject isInstance(PythonObject instance, PythonObject cls) {
        if (hasattr.call(cls, newString("__instancecheck__")).toBoolean()) {
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

    public static PythonObject issubclass(PythonObject cls, PythonObject base) {
        return base.callTypeAttribute("__subclasscheck__", cls);
    }
}
