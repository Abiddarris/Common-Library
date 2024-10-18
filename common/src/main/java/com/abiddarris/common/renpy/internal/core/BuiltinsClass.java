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

import static com.abiddarris.common.renpy.internal.Builtins.enumerate;
import static com.abiddarris.common.renpy.internal.Builtins.list;
import static com.abiddarris.common.renpy.internal.Builtins.range;
import static com.abiddarris.common.renpy.internal.Builtins.zip;

import com.abiddarris.common.renpy.internal.PythonObject;

public class BuiltinsClass {

    public static PythonObject zip(PythonObject... iterables) {
        return zip.call(iterables);
    }

    public static PythonObject list(PythonObject iterable) {
        return list.call(iterable);
    }

    public static PythonObject range(PythonObject start, PythonObject stop, PythonObject step) {
        return range.call(start, stop, step);
    }

    public static PythonObject enumerate(PythonObject iterator) {
        return enumerate.call(iterator);
    }

}