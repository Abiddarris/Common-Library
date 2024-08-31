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

import com.abiddarris.common.renpy.internal.PythonFunction;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.PythonTuple;
import com.abiddarris.common.renpy.internal.object.PythonMethod;
import java.util.HashMap;
import java.util.Map;

public class AttributeManager {

    private CriticalAttribute criticalAttribute = new CriticalAttribute();
    private PythonObject owner;
    private AttributeHolder attributes = new BootstrapAttributeHolder();

    public AttributeManager(PythonObject owner) {
        this.owner = owner;
    }
    
    public PythonObject get(String name) {
        PythonObject attribute = criticalAttribute.getAttribute(name);
        if (attribute != null) {
            return attribute;
        }
        return attributes.get(name);
    }
    
    public void put(String name, PythonObject attribute) {
        if(criticalAttribute.setAttribute(name, attribute)) {
            return;
        }
        attributes.store(name, attribute);
    }
    
    public PythonObject findAttribute(String name) {
        PythonObject attribute = findAttributeWithoutType(name);
        if (attribute != null) {
            return attribute;
        }

        PythonObject type = criticalAttribute.getType();

        return findAttributeWithoutTypeAllowConversion(type, name);
    }

    public PythonObject findAttributeWithoutTypeAllowConversion(PythonObject type, String name) {
        PythonObject attribute = findAttributeWithoutType(type, name);
        if (attribute instanceof PythonFunction) {
            return new PythonMethod(owner, attribute);
        }

        return attribute;
    }

    public PythonObject findAttributeWithoutType(String name) {
        return findAttributeWithoutType(owner, name);
    }
    
    private static PythonObject findAttributeWithoutType(PythonObject type, String name) {
        PythonObject attribute = type.getAttributes().get(name);
        if (attribute != null) {
            return attribute;
        }

        PythonTuple bases = (PythonTuple)type.getAttributes().get("__bases__");
        if (bases != null) {
            for (var element : bases.getElements()) {
                attribute = findAttributeWithoutType(element, name);
                if (attribute != null) {
                    return attribute;
                }
            }
        }

        return null;
    }
}
