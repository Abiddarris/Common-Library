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
package com.abiddarris.common.renpy.internal.mod.operator;

import static com.abiddarris.common.renpy.internal.mod.operator.OperatorImpl.operator;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

public class ItemGetterImpl {

    static void init() {
        ClassDefiner definer = operator.defineClass("itemgetter");
        definer.defineFunction("__init__", ItemGetterImpl::init0, "self", "item");
        definer.defineFunction("__call__", ItemGetterImpl::call, "self", "obj");

        definer.define();
    }

    private static void
    init0(PythonObject self, PythonObject item) {
        self.setAttribute("item", item);
    }

    private static PythonObject
    call(PythonObject self, PythonObject obj) {
        return obj.getItem(self.getAttribute("item"));
    }
}
