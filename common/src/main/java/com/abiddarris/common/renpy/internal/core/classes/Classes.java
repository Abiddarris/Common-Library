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
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.core.classes.AttributeSetter.setAttributes;
import static com.abiddarris.common.renpy.internal.core.classes.DelegateType.delegateInit;
import static com.abiddarris.common.renpy.internal.core.classes.DelegateType.delegateNew;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.PythonTuple;
import com.abiddarris.common.renpy.internal.core.Types;
import com.abiddarris.common.renpy.internal.attributes.AttributeHolder;

import java.util.LinkedHashSet;
import java.util.Set;

public class Classes {

    static PythonObject newClass(PythonObject cls, PythonObject args) {
        return newClass(cls, args, null);
    }

    public static PythonObject newClass(PythonObject cls, PythonObject args, AttributeHolder attributeHolder) {
        PythonObject name = args.getItem(newInt(0));
        PythonObject bases = args.getItem(newInt(1));

        PythonObject delegateResult = delegateNew(cls, name, bases, args);
        if (delegateResult != null)
            return delegateResult;

        if (bases.length() == 0) {
            bases = newTuple(object);
        }

        // FIXME: This will be a problem if the tuple is not default tuple

        PythonObject self = new PythonObject(attributeHolder);
        self.setAttributeDirectly("__class__", cls);
        self.setAttribute("__name__", name);
        self.setAttribute("__bases__", bases);

        setAttributes(self, args);

        Set<PythonObject> mro = new LinkedHashSet<>();
        mro.add(self);
        for (PythonObject parent : ((PythonTuple) bases).getElements()) {
            PythonObject[] parentMro = ((PythonTuple) parent.getAttributes().get("__mro__")).getElements();
            for (PythonObject mro0 : parentMro) {
                mro.remove(mro0);
                mro.add(mro0);
            }
        }

        self.setAttribute("__mro__", newTuple(mro.toArray(PythonObject[]::new)));

        return self;
    }

    private static PythonObject typeNew(PythonObject cls, PythonObject args) {
        if (args.length() == 1) {
            return Types.type(args.getItem(newInt(0)));
        }

        if (args.length() != 3) {
            TypeError.call().raise();
        }

        PythonObject self = newClass(cls, args);
        return self;
    }

    private static void typeInit(PythonObject self, PythonObject args) {
        if (args.length() == 1) {
            return;
        }

        delegateInit(self, args);
    }

}
