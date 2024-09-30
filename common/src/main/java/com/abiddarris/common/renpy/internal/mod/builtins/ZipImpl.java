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
import static com.abiddarris.common.renpy.internal.Python.newTuple;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

class ZipImpl {

    static void define() {
        ClassDefiner definer = builtins.defineClass("zip");
        definer.defineFunction("__init__", ZipImpl::init, "self", "*iterables");
        definer.defineFunction("__iter__", ZipImpl::iter, "self");
        definer.defineFunction("__next__", ZipImpl::next, "self");

        definer.define();
    }

    private static void init(PythonObject self, PythonObject iterables) {
        PythonObject iterable0 = newList();
        int count = 0;
        for (PythonObject iterable : iterables) {
            count++;
            iterable0.callAttribute("append", iterable.callAttribute("__iter__"));
        }

        self.setAttribute("iterables", iterable0);
        self.setJavaAttribute("length", count);
    }

    private static PythonObject
    iter(PythonObject self) {
        return self;
    }

    private static PythonObject
    next(PythonObject self) {
        PythonObject[] objects = new PythonObject[(int) self.getJavaAttribute("length")];
        int i = 0;
        for (PythonObject iterator : self.getAttribute("iterables")) {
            objects[i++] = iterator.callAttribute("__next__");
        }

        return newTuple(objects);
    }
}
