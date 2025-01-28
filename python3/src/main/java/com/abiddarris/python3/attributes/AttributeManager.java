/***********************************************************************************
 * Copyright 2024 - 2025 Abiddarris
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

import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Builtins.object;

import com.abiddarris.common.utils.WeakCollection;
import com.abiddarris.python3.PythonFunction;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.PythonTuple;
import com.abiddarris.python3.attributes.DisableAttributeAccessOptimizationEvent.Type;
import com.abiddarris.python3.object.PropertyObject;
import com.abiddarris.python3.object.PythonMethod;
import com.abiddarris.common.utils.WeakableValueMap;

public class AttributeManager {

    private boolean optimizeGetter = true;
    private boolean optimizeSetter = true;
    private CriticalAttribute criticalAttribute = new CriticalAttribute();
    private PythonObject owner;
    private AttributeHolder attributes;

    private final WeakCollection<PythonObject> classes = new WeakCollection<>();
    private final WeakCollection<PythonObject> instances = new WeakCollection<>();

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

    public boolean isOptimizeGetter() {
        return optimizeGetter;
    }

    public boolean isOptimizeSetter() {
        return optimizeSetter;
    }

    public void registerSubclass(PythonObject Class) {
        classes.add(Class);
    }

    public void registerInstance(PythonObject instance) {
        if (instance == owner) {
            return;
        }
        instances.add(instance);
    }

    public void notifySubclasses(Event event) {
        classes.forEach(Class -> Class.getAttributes().onEvent(event));
    }

    protected void onEvent(Event event) {
        if (event instanceof DisableAttributeAccessOptimizationEvent) {
            DisableAttributeAccessOptimizationEvent ev = (DisableAttributeAccessOptimizationEvent)event;
            switch (ev.getType()) {
                case GETTER :
                    optimizeGetter = false;
                    break;
                case SETTER :
                    optimizeSetter = false;
            }
        }

        notifySubclasses(event);
    }

    public void put(String name, PythonObject attribute) {
        if(criticalAttribute.setAttribute(owner, name, attribute)) {
            return;
        }
        attributes.store(name, attribute);

        Type type = null;
        if (name.equals("__getattribute__")) {
            type = Type.GETTER;
            optimizeGetter = false;
        } else if (name.equals("__setattr__")){
            type = Type.SETTER;
            optimizeSetter = false;
        } else {
            return;
        }

        notifySubclasses(new DisableAttributeAccessOptimizationEvent(type));
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
