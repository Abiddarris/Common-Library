/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
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
package com.abiddarris.python3.mod.builtins;

import static com.abiddarris.python3.Builtins.ValueError;
import static com.abiddarris.python3.Builtins.builtins;
import static com.abiddarris.python3.Python.newList;
import static com.abiddarris.python3.Python.newString;

import com.abiddarris.python3.PythonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
        builtins.defineFunction("max", BuiltinsImpl::max, "iterable", "key");

        SetImpl.define();
        GeneratorImpl.define();
        ZipImpl.define();
        RangeImpl.define();
        BytesImpl.define();
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

    public static PythonObject max(PythonObject iterable, PythonObject key) {
        Iterator<PythonObject> jIterable = iterable.iterator();
        if (!jIterable.hasNext()) {
            ValueError.call(newString("max() iterable argument is empty")).raise();
        }

        PythonObject candidate = jIterable.next();
        PythonObject candidateKey = key.call(candidate);
        while (jIterable.hasNext()) {
            PythonObject element = jIterable.next();
            PythonObject elementKey = key.call(element);

            if (elementKey.jGreaterThan(candidateKey)) {
                candidate = element;
                candidateKey = elementKey;
            }
        }

        return candidate;
    }
}
