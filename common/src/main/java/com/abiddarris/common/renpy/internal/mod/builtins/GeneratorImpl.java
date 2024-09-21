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

import static com.abiddarris.common.renpy.internal.PythonObject.StopIteration;
import static com.abiddarris.common.renpy.internal.PythonObject.builtins;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.gen.GeneratorObject;

class GeneratorImpl {

    static PythonObject define() {
        ClassDefiner definer = builtins.defineClass("generator");
        definer.defineFunction("__iter__", GeneratorImpl.class, "iter", "self");
        definer.defineFunction("__next__", GeneratorImpl.class, "next", "self");

        return definer.define();
    }

    private static PythonObject
    iter(GeneratorObject self) {
        return self;
    }

    private static PythonObject
    next(GeneratorObject self) {
        PythonObject res = self.next();
        if (res == null) {
            StopIteration.call().raise();
        }

        return res;
    }

}
