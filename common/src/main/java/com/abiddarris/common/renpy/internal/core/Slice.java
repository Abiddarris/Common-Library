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
import static com.abiddarris.common.renpy.internal.PythonObject.None;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

public class Slice {

    private static boolean init;
    private static PythonObject slice;

    public static PythonObject define(PythonObject builtins) {
        if (init) {
            return slice;
        }
        init = true;

        ClassDefiner definer = builtins.defineClass("slice");
        definer.defineFunction("__init__", Slice.class, "init", "self", "start", "stop", "step");

        return slice = definer.define();
    }

    private static void init(PythonObject self, PythonObject start, PythonObject stop, PythonObject step) {
        self.setAttribute("start", start);
        self.setAttribute("stop", stop);
        self.setAttribute("step", step);
    }

    public static PythonObject newSlice(int start) {
        return slice.call(newInt(start), None, None);
    }

    public static PythonObject newSlice(PythonObject start) {
        return newSlice(start, None);
    }

    public static PythonObject newSlice(PythonObject start, PythonObject end) {
        return slice.call(start, end, None);
    }
}
