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

import static com.abiddarris.python3.Builtins.builtins;
import static com.abiddarris.python3.Builtins.list;
import static com.abiddarris.python3.Builtins.str;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.core.Functions;

public class BytesImpl {

    static void define() {
        ClassDefiner definer = builtins.defineClass("bytes");
        definer.defineFunction("__init__", BytesImpl::init, "self", "iterable_of_int");
        definer.defineFunction("__iter__", BytesImpl::iter, "self");
        definer.defineFunction("__len__", BytesImpl::len, "self");
        definer.defineFunction("__str__", BytesImpl::str, "self");

        definer.define();
    }

    private static PythonObject len(PythonObject self) {
        return Functions.len(self.getAttribute("__data__"));
    }

    private static PythonObject iter(PythonObject self) {
        return self.getAttribute("__data__").callAttribute("__iter__");
    }

    private static void init(PythonObject self, PythonObject iterableOfInt) {
        self.setAttribute("__data__", list.call(iterableOfInt));
    }

    private static PythonObject str(PythonObject self) {
        return str.call(self.getAttribute("__data__"));
    }

}
