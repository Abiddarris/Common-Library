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

import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.object;

import com.abiddarris.common.renpy.internal.PythonFunction;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.PythonTuple;
import com.abiddarris.common.renpy.internal.object.PropertyObject;
import com.abiddarris.common.renpy.internal.object.PythonMethod;

public class AttributeManager {

    private CriticalAttribute criticalAttribute = new CriticalAttribute();
    private PythonObject owner;
    private AttributeHolder attributes;
    
    public AttributeManager(PythonObject owner) {
        this(owner, new BootstrapAttributeHolder());
    }
    
    public AttributeManager(PythonObject owner, AttributeHolder holder) {
        this.owner = owner;
        
        attributes = holder;
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
        attribute = findAttributeWithoutTypeAllowConversion(type, name);
        if (attribute != null){
            return attribute;
        }

        PythonObject getattr = findAttributeWithoutTypeAllowConversion(type, "__getattr__");
        if (getattr == null) {
            return null;
        }

        return getattr.call(newString(name));
    }

    public PythonObject findAttributeWithoutTypeAllowConversion(PythonObject type, String name) {
        PythonObject attribute = type.getAttributes()
                .findAttributeWithoutType(name);
        attribute = processAttribute(attribute);

        return attribute;
    }

    public PythonObject findAttributeWithoutType(String name) {
        PythonTuple mro = (PythonTuple)get("__mro__");

        if (mro == null) {
            PythonObject attribute = get(name);
            return attribute;
        }

        for (PythonObject parent : mro.getElements()) {
            PythonObject attribute = parent.getAttributes().get(name);
            if (attribute != null) {
                return attribute;
            }
        }

        return null;
    }
    
    public PythonObject searchAttribute(PythonObject startClass, PythonObject instanceClass, String name) {
        PythonObject attribute = searchAttributeInternal(startClass, instanceClass, name);
        attribute = processAttribute(attribute);
        
        return attribute;
    }
    
    private PythonObject processAttribute(PythonObject attribute) {
        if (attribute instanceof PythonFunction) {
            return new PythonMethod(owner, attribute);
        } else if (attribute instanceof PropertyObject) {
            PythonObject fget = attribute.getAttribute("fget");
            PythonObject method = new PythonMethod(owner, fget);

            return method.call();
        }
        
        return attribute;
    }
    
    private PythonObject searchAttributeInternal(PythonObject startClass, PythonObject instanceClass, String name) {
        if (startClass == object) {
            return null;
        }
        
        AttributeManager attributeManager = instanceClass.getAttributes();
        PythonTuple mro = (PythonTuple)attributeManager.get("__mro__");
        
        if (mro == null) {
            // FIXME: instanceClass is not class if MRO is null
        }
        
        boolean startSearch = false;
        for (PythonObject parent : mro.getElements()) {
            if (parent == startClass) {
                startSearch = true;
                continue;
            }
            
            if (!startSearch) {
                continue;
            }
            
            PythonObject attribute = parent.getAttributes().get(name);
            if (attribute != null) {
                return attribute;
            }
        }
        
        return null;
    }

}
