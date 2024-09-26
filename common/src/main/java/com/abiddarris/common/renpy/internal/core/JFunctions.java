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

import static com.abiddarris.common.renpy.internal.Python.tryExcept;
import static com.abiddarris.common.renpy.internal.Builtins.AttributeError;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.utils.ObjectWrapper;

public class JFunctions {

    public static boolean hasattr(PythonObject obj, String name) {
        ObjectWrapper hasAttribute = new ObjectWrapper<>(true);
        tryExcept(() -> obj.getAttribute(name.toString())).
                onExcept((e) -> hasAttribute.setObject(false), AttributeError).execute();

        return (boolean) hasAttribute.getObject();
    }

}
