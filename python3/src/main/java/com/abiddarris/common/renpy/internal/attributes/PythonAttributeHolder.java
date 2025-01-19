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
package com.abiddarris.common.renpy.internal.attributes;

import static com.abiddarris.common.renpy.internal.Builtins.dict;
import static com.abiddarris.common.renpy.internal.Python.*;
import static com.abiddarris.common.renpy.internal.core.Types.type;

import com.abiddarris.common.renpy.internal.PythonDict;
import com.abiddarris.common.renpy.internal.PythonException;
import com.abiddarris.common.renpy.internal.PythonObject;

public class PythonAttributeHolder implements AttributeHolder {
    
    private PythonObject attributes = newDict();
    
    @Override
    public void store(String name, PythonObject value) {
        attributes.setItem(newString(name), value);
    }
    
    @Override
    public PythonObject get(String name) {
        if (name.equals("__dict__")) {
            return attributes;
        }

        if (type(attributes) == dict) {
            return ((PythonDict)attributes).getMap().get(newString(name));
        }
        try {
            // FIXME: Ugly trick, in the future this trick could fuck we up!
            return attributes.getItem(newString(name));
        } catch (PythonException e) {
        }
        
        return null;
    }
    
}
