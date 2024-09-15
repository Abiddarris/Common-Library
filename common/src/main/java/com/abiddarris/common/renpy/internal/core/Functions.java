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

import static com.abiddarris.common.renpy.internal.PythonObject.AttributeError;
import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.hasattr;
import static com.abiddarris.common.renpy.internal.PythonObject.newBoolean;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.tuple;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.utils.ObjectWrapper;

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
        ObjectWrapper<PythonObject> hasAttribute = new ObjectWrapper<>(True);
        tryExcept(() -> obj.getAttribute(name.toString())).
                onExcept((e) -> hasAttribute.setObject(False), AttributeError).execute();

        return hasAttribute.getObject();
    }

    public static PythonObject hash(PythonObject obj) {
        return obj.callTypeAttribute("__hash__");
    }
}
