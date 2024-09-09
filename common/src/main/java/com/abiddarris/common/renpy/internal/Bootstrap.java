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
package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.object;

import static java.util.Arrays.asList;

import com.abiddarris.common.renpy.internal.core.Types;
import com.abiddarris.common.renpy.internal.model.AttributeHolder;

import java.util.LinkedHashSet;
import java.util.Set;

class Bootstrap {
    
    static PythonObject newClass(PythonObject cls, PythonObject args) {
        return newClass(cls, args, null);
    }
    
    static PythonObject newClass(PythonObject cls, PythonObject args, AttributeHolder attributeHolder) {
        return Types.newClass(cls, args, attributeHolder);
    }
    
}
