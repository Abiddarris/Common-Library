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

import static com.abiddarris.common.renpy.internal.PythonObject.*;

import com.abiddarris.common.renpy.internal.PythonObject;

public class Errors {
    
    public static void raiseAttributeError(PythonObject object, PythonObject attributeName) {
        String message = isinstance.call(object, type).toBoolean() ? "type object %s" : "%s object";
        message += " has no attribute %s";
            
        AttributeError.call(newString(String.format(
            message, object.getAttribute("__name__"), attributeName
        ))).raise();
    }
    
    
}
