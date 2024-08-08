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

import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.AttributeError;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.newBoolean;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

public class BuiltinsImpl {
    
    private static PythonObject len(PythonObject obj) {
        return obj.callTypeAttribute("__len__");
    }
    
    private static PythonObject boolNew(PythonObject cls, PythonObject obj) {
        ObjectWrapper<PythonObject> returnValue = new ObjectWrapper<>();
        tryExcept(() -> {
            returnValue.setObject(obj.callTypeAttribute("__bool__"));
        }).onExcept((e) -> {
            tryExcept(() -> {
                returnValue.setObject(newBoolean(
                                obj.callTypeAttribute("__len___")
                                   .toInt() != 0));
            }).onExcept((e1) -> {
                returnValue.setObject(True);
            }, AttributeError);
        }, AttributeError).execute();
        
        return returnValue.getObject();
    }
    
    private static void typeInit(PythonObject self, PythonObject args) {
    }
    
}
