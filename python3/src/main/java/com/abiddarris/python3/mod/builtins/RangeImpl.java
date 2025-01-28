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
package com.abiddarris.python3.mod.builtins;

import static com.abiddarris.python3.Builtins.StopIteration;
import static com.abiddarris.python3.Builtins.builtins;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;

public class RangeImpl {

    static void define() {
        ClassDefiner definer = builtins.defineClass("range");
        definer.defineFunction("__init__", RangeImpl::init, "self", "start", "stop", "step");
        definer.defineFunction("__iter__", RangeImpl::iter, "self");
        definer.defineFunction("__next__", RangeImpl::next, "self");

        definer.define();
    }

    private static void
    init(PythonObject self, PythonObject start,
         PythonObject stop, PythonObject step) {

        self.setAttribute("num", start);
        self.setAttribute("stop", stop);
        self.setAttribute("step", step);
    }


    private static PythonObject
    iter(PythonObject self) {
        return self;
    }

    private static PythonObject
    next(PythonObject self) {
        PythonObject num = self.getAttribute("num");
        PythonObject step = self.getAttribute("step");
        PythonObject stop = self.getAttribute("stop");

        boolean isPositive = step.jGreaterThan(-1);
        if (isPositive && num.jGreaterEquals(stop) || !isPositive && num.jLessThan(stop)) {
            StopIteration.call().raise();
        }

        PythonObject nextNum = num.add(step);
        self.setAttribute("num", nextNum);

        return num;
    }
}
