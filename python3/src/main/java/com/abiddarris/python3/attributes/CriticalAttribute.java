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
package com.abiddarris.python3.attributes;

import static com.abiddarris.python3.Builtins.TypeError;
import static com.abiddarris.python3.Python.newString;

import com.abiddarris.python3.PythonObject;

public class CriticalAttribute {

    private PythonObject class0;

    public PythonObject getAttribute(String key) {
        if (key.equals("__class__")) {
            return class0;
        }
        return null;
    }
    
    public boolean setAttribute(PythonObject owner, String key, PythonObject value) {
        if (key.equals("__class__")) {
            if (class0 != null) {
                TypeError.call(newString("class already setted")).raise();
            }

            value.getAttributes().registerInstance(owner);
            class0 = value;
            return true;
        }
        return false;
    }
    
    public PythonObject getType() {
        return this.class0;
    }

}
