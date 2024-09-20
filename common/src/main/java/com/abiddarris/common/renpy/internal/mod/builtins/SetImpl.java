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

import static com.abiddarris.common.renpy.internal.PythonObject.builtins;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.object.SetObject;

import java.util.Set;

class SetImpl {

    static void define() {
        ClassDefiner definer = builtins.defineClass("set");
        definer.defineFunction("__new__", SetImpl.class, "new0", "cls", "args");
        definer.defineFunction("__init__", SetImpl.class, "init", "cls", "args");
        definer.define();
    }

    private static PythonObject new0(PythonObject cls, PythonObject args) {
        return new SetObject(cls);
    }

    private static void init(SetObject self, PythonObject args) {
        Set<PythonObject> set = self.getSet();
        for (PythonObject element : args) {
            set.add(args);

       }

    }

}