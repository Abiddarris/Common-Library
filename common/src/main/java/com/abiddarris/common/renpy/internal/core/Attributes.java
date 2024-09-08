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

import static java.util.regex.Pattern.quote;

import com.abiddarris.common.renpy.internal.PythonObject;

public class Attributes {

    public static PythonObject getNestedAttribute(PythonObject target, String attributePath) {
        String[] paths = attributePath.split(quote("."));
        for (String path : paths) {
            if (path.isBlank()) {
                throw new IllegalArgumentException(String.format("Broken attribute path %s", attributePath));
            }
            target = target.getAttribute(path);
        }

        return target;
    }

    public static PythonObject callNestedAttribute(PythonObject target, String attributePath, PythonObject... args) {
        return getNestedAttribute(target, attributePath).call(args);
    }

}
