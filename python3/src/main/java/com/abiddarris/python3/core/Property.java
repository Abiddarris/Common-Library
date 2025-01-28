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
package com.abiddarris.python3.core;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.object.PropertyObject;

public class Property {

    private static PythonObject property;
    private static boolean init;

    public static PythonObject define(PythonObject builtins) {
        if (init) {
            return property;
        }
        init = true;

        ClassDefiner definer = builtins.defineClass("property");
        definer.defineFunction("__new__", Property.class, "new0", "cls", "fget");
        definer.defineFunction("__init__", Property.class, "init", "self", "fget");

        return property = definer.define();
    }

    private static PythonObject new0(PythonObject cls, PythonObject fget) {
        return new PropertyObject(cls);
    }

    private static void init(PythonObject self, PythonObject fget) {
        self.setAttribute("fget", fget);
    }

    public static PythonObject property(PythonObject fget) {
        return property.call(fget);
    }

}
