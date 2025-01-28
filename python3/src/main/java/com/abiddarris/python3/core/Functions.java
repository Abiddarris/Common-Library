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

import static com.abiddarris.python3.Builtins.AttributeError;
import static com.abiddarris.python3.Builtins.False;
import static com.abiddarris.python3.Builtins.True;
import static com.abiddarris.python3.Builtins.TypeError;
import static com.abiddarris.python3.Builtins.hasattr;
import static com.abiddarris.python3.PythonObject.newBoolean;
import static com.abiddarris.python3.PythonObject.newString;
import static com.abiddarris.python3.PythonObject.tryExcept;
import static com.abiddarris.python3.Builtins.tuple;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.common.utils.ObjectWrapper;

public class Functions {
    
    private static PythonObject isInstanceBootstrap(PythonObject instance, PythonObject cls) {
        return cls.callTypeAttribute("__instancecheck__", instance);
    }

    public static PythonObject isinstance(PythonObject instance, PythonObject cls) {
        return isInstance(instance, cls);
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

    public static PythonObject bool(PythonObject obj) {
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

    public static PythonObject any(PythonObject iterable) {
        for (PythonObject element : iterable) {
            if (element.toBoolean()) {
                return True;
            }
        }
        return False;
    }

    public static PythonObject hasattr(PythonObject obj, PythonObject name) {
        return newBoolean(JFunctions.hasattr(obj, name.toString()));
    }

    public static PythonObject hash(PythonObject obj) {
        return obj.callTypeAttribute("__hash__");
    }

    public static PythonObject len(PythonObject obj) {
        return obj.callTypeAttribute("__len__");
    }

    public static PythonObject all(PythonObject iterable) {
        for (PythonObject element : iterable) {
            if (!element.toBoolean()) {
                return False;
            }
        }
        return True;
    }

    public static PythonObject not(PythonObject obj) {
        return newBoolean(!obj.toBoolean());
    }

    public static PythonObject max(PythonObject obj, PythonObject obj2) {
        return obj.jGreaterThan(obj2) ? obj : obj2;
    }

}
