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
package com.abiddarris.common.renpy.internal.core.classes;

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.core.Functions.issubclass;
import static com.abiddarris.common.renpy.internal.core.Types.type;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.PythonTuple;

public class DelegateType {

    private static boolean allowDelegate;

    public static void activate() {
        allowDelegate = true;
    }

    static PythonObject delegateNew(PythonObject cls, PythonObject name, PythonObject bases, PythonObject args) {
        if (!allowDelegate) {
            return null;
        }

        PythonObject type = getType(cls, bases);
        if (type != cls) {
            PythonObject attributes = args.getItem(newInt(2));

            return type.callAttribute("__new__", type, name, bases, attributes);
        }

        return null;
    }

    static PythonObject delegateInit(PythonObject self, PythonObject args) {
        if (!allowDelegate) {
            return None;
        }

        PythonObject cls = type(self);
        PythonObject bases = args.getItem(newInt(1));

        PythonObject type = getType(cls, bases);
        if (type != cls) {
            PythonObject name = args.getItem(newInt(0));
            PythonObject attributes = args.getItem(newInt(2));

            return type.callAttribute("__init__", type, name, bases, attributes);
        }

        return None;
    }

    private static PythonObject getType(PythonObject choosed, PythonObject bases) {
        if (!bases.toBoolean()) {
            return choosed;
        }

        for (PythonObject cls : ((PythonTuple)bases).getElements()) {
            PythonObject meta = type(cls);
            if (issubclass(meta, choosed).toBoolean()) {
                choosed = meta;
            } else if (!issubclass(choosed, meta).toBoolean()) {
                TypeError.call(newString("metaclass conflict: the metaclass of a derived class must be a (non-strict) subclass of the metaclasses of all its bases")).raise();
            }
        }

        return choosed;
    }
}
