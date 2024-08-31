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
package com.abiddarris.common.renpy.internal.model;

import com.abiddarris.common.renpy.internal.PythonObject;

public class CriticalAttribute {

    private PythonObject class0;

    public PythonObject getAttribute(String key) {
        if (key.equals("__class__")) {
            return class0;
        }
        return null;
    }
    
    public boolean setAttribute(String key, PythonObject value) {
        if (key.equals("__class__")) {
            class0 = value;
            return true;
        }
        return false;
    }
    
    public PythonObject getType() {
        return this.class0;
    }

}
