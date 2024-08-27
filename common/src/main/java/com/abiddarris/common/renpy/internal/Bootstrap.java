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

class Bootstrap {
    
    static PythonObject newClass(PythonObject cls, PythonObject args) {
        PythonObject name = args.getItem(newInt(0));
        PythonObject bases = args.getItem(newInt(1));
        
        if(bases.length() == 0) {
            bases = newTuple(object);
        }
        
        PythonObject self = new PythonObject();
        self.setAttribute("__class__", cls);
        self.setAttribute("__name__", name);
        self.setAttribute("__bases__", bases);
       
        return self;
    }
    
    static PythonObject isInstanceBootstrap(PythonObject instance, PythonObject cls) {
        return cls.callTypeAttribute("__instancecheck__", instance);
    }
    
}
