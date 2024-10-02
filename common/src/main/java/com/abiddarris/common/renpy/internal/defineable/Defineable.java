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
package com.abiddarris.common.renpy.internal.defineable;

import static com.abiddarris.common.renpy.internal.core.functions.Functions.newFunction;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.core.functions.PFunction;
import com.abiddarris.common.renpy.internal.core.functions.V1Function;
import com.abiddarris.common.renpy.internal.core.functions.V2Function;
import com.abiddarris.common.renpy.internal.core.functions.V5Function;

public interface Defineable {

    PythonObject defineAttribute(String name, PythonObject attribute);
    PythonObject getModuleName();

    default PythonObject defineFunction(String name, PFunction function, String... argumentNames) {
        return initFunction(name, newFunction(function, argumentNames));
    }

    default PythonObject defineFunction(String name, PythonObject decorator, V2Function function, String... argumentNames) {
        return initFunction(name, decorator.call(newFunction(function, argumentNames)));
    }

    default PythonObject defineFunction(String name, V1Function function, String... argumentNames) {
        return initFunction(name, newFunction(function, argumentNames));
    }

    default PythonObject defineFunction(String name, V2Function function, String... argumentNames) {
        return initFunction(name, newFunction(function, argumentNames));
    }

    default PythonObject defineFunction(String name, V5Function function, String... argumentNames) {
        return initFunction(name, newFunction(function, argumentNames));
    }

    private PythonObject initFunction(String name, PythonObject function) {
        function.setAttribute("__module__", getModuleName());

        return defineAttribute(name, function);
    }

}
