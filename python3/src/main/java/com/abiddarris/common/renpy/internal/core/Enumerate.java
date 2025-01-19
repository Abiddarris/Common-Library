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

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newTuple;

import com.abiddarris.common.renpy.internal.Python;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

public class Enumerate {

    private static boolean init;

    public static PythonObject define(PythonObject builitins) {
        if (init) {
            return null;
        }
        init = true;

        ClassDefiner definer = builitins.defineClass("enumerate");
        definer.defineFunction("__init__", Enumerate.class, "init", "self", "iterable");
        definer.defineFunction("__iter__", Enumerate.class, "iter", "self");
        definer.defineFunction("__next__", Enumerate.class, "next", "self");
        return definer.define();
    }

    private static void init(PythonObject self, PythonObject iterable) {
        self.setAttribute("__iterator__", iterable.callAttribute("__iter__"));
        self.setAttribute("__index__", newInt(0));
    }

    private static PythonObject iter(PythonObject self) {
        return self;
    }

    private static PythonObject next(PythonObject self) {
        PythonObject iterator = self.getAttribute("__iterator__");
        PythonObject index = self.getAttribute("__index__");

        PythonObject element = iterator.callAttribute("__next__");
        PythonObject elements = newTuple(index, element);

        self.setAttribute("__index__", newInt(index.toInt() + 1));

        return elements;
    }
}
