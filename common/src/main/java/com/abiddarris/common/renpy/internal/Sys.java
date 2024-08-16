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

import static com.abiddarris.common.renpy.internal.PythonObject.newDict;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;

import com.abiddarris.common.annotations.PrivateApi;

@PrivateApi
class Sys {
    static final int maxsize = Integer.MAX_VALUE;
    
    static PythonObject sys;
    
    static void init() {
        sys = Types.ModuleType.call(newString("sys"));
        sys.setAttribute("modules", newDict(newString("sys"), sys));
    }
}
