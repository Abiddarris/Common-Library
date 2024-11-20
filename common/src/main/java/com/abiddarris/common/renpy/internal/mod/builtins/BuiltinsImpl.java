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
package com.abiddarris.common.renpy.internal.mod.builtins;

import static com.abiddarris.common.renpy.internal.Builtins.builtins;
import static com.abiddarris.common.renpy.internal.Python.newList;

import com.abiddarris.common.renpy.internal.Python;
import com.abiddarris.common.renpy.internal.PythonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BuiltinsImpl {

    private static boolean init;

    public static void initRest() {
        if (init) {
            return;
        }

        init = true;

        builtins.defineFunction("hash", BuiltinsImpl::hash, "self");
        builtins.defineFunction("sorted", BuiltinsImpl::sorted, "iterable", "key");

        SetImpl.define();
        GeneratorImpl.define();
        ZipImpl.define();
        RangeImpl.define();
    }

    private static PythonObject hash(PythonObject obj) {
        return obj.callTypeAttribute("__hash__");
    }

    private static PythonObject sorted(PythonObject iterable, PythonObject key) {
        List<PythonObject> sortedElements = new ArrayList<>();
        for (PythonObject element : iterable) {
            sortedElements.add(element);
        }

        Collections.sort(sortedElements, (p1, p2) -> {
            p1 = key.call(p1);
            p2 = key.call(p2);

            if (p1.jLessThan(p2)) {
                return -1;
            }

            if (p1.equals(p2)) {
                return 0;
            }

            return 1;
        });

        return newList(sortedElements);
    }
}
