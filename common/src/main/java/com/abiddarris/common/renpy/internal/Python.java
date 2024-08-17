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

import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.int0;
import static com.abiddarris.common.renpy.internal.PythonObject.str;

import static java.util.Arrays.asList;

import com.abiddarris.common.renpy.internal.trycatch.ExceptFinally;

import java.util.ArrayList;
import java.util.List;

public class Python {
    
    Python() {}
    
    public static PythonObject newString(String string) {
        PythonString object = new PythonString(string);
        object.setAttribute("__class__", str);
        
        return object;
    }
    
    public static PythonObject newInt(int value) {
        PythonObject object = new PythonInt(value);
        object.setAttribute("__class__", int0);

        return object;
    }
    
    public static PythonObject newBoolean(boolean val) { 
        return val ? True : False;
    }
    
    public static PythonObject newList(PythonObject... elements) {
        return newList(asList(elements));
    }
    
    public static PythonObject newList(List<PythonObject> elements) {
        return new PythonList(new ArrayList<>(elements));
    }
    
    public static ExceptFinally tryExcept(Runnable tryRunnable) {
        return new ExceptFinally(tryRunnable);
    }
}
