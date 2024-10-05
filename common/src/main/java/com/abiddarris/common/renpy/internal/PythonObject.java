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

import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.Types.ModuleType;
import static com.abiddarris.common.renpy.internal.core.Errors.raiseAttributeError;
import static com.abiddarris.common.renpy.internal.core.JFunctions.jIsinstance;
import static com.abiddarris.common.renpy.internal.core.Slice.newSlice;
import static com.abiddarris.common.renpy.internal.imp.Imports.importFrom;

import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.builder.ModuleTarget;
import com.abiddarris.common.renpy.internal.core.Attributes;
import com.abiddarris.common.renpy.internal.defineable.Defineable;
import com.abiddarris.common.renpy.internal.imp.PythonObjectLoadTarget;
import com.abiddarris.common.renpy.internal.attributes.AttributeHolder;
import com.abiddarris.common.renpy.internal.attributes.AttributeManager;
import com.abiddarris.common.renpy.internal.attributes.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.attributes.PythonAttributeHolder;
import com.abiddarris.common.renpy.internal.object.PythonMethod;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;
import com.abiddarris.common.utils.ObjectWrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

public class PythonObject extends Python implements Defineable, Iterable<PythonObject> {

    private static PythonObject pythonObjectNew(PythonObject cls) {
        PythonObject instance = new PythonObject();
        instance.setAttributeDirectly("__class__", cls);

        return instance;
    }
    
    private static PythonObject objectHash(PythonObject self) {
        return newPythonInt(self.getHashCode());
    }
    
    static PythonObject typeGetAttribute(PythonObject self, PythonObject name) {
        long a = System.currentTimeMillis();

        PythonObject attribute = self.attributes.findAttribute(name.toString());
        if(attribute == null) {
            raiseAttributeError(self, name);
        }

        return attribute;
    }
    
    AttributeManager attributes;

    private Map<String, Object> javaAttributes = new HashMap<>();

    public PythonObject(AttributeHolder holder) {
        this(holder, null);
    }

    public PythonObject(AttributeHolder holder, PythonObject cls) {
        if(holder == null) {
            holder = new PythonAttributeHolder();
        } 
        
        attributes = new AttributeManager(this, holder);

        setAttributeDirectly("__class__", cls);
    }

    public PythonObject(PythonObject cls) {
        this(null, cls);
    }
    
    public PythonObject() {
        this((AttributeHolder)null);
    }
    
    public AttributeManager getAttributes() {
        return attributes;
    }
    
    public void addMethod(String name, PythonMethod func) {
        setAttribute(name, func);
    }

    public void setAttribute(String name, PythonObject obj) {
        setAttribute(newString(name), obj);
    }

    public void setAttribute(PythonObject name, PythonObject value) {
        callTypeAttribute("__setattr__", name, value);
    }

    public void setAttributeDirectly(String name, PythonObject value) {
        attributes.put(name, value);
    }
    
    public void setJavaAttribute(String name, Object object) {
        javaAttributes.put(name, object);
    }

    public <T> T getJavaAttribute(String name) {
        return (T) javaAttributes.get(name);
    }

    public PythonObject getAttribute(String name) {
        PythonObject attribute = callTypeAttribute("__getattribute__",
            new PythonParameter()
                .addPositionalArgument(newPythonString(name)));
        
        return attribute;
    }
    
    public PythonObject getAttribute(String name, PythonObject defaultValue) {
        ObjectWrapper<PythonObject> returnValue = new ObjectWrapper<>(defaultValue);
        tryExcept(() -> returnValue.setObject(getAttribute(name))).
        onExcept((e) -> {}, Builtins.AttributeError).execute();
        
        return returnValue.getObject();
    }

    public boolean getAttributeJB(String name) {
        return getAttribute(name).toBoolean();
    }

    public PythonObject callAttribute(String name, PythonParameter parameter) {
        PythonObject object = getAttribute(name);
        return object.call(parameter);
    }
    
    public PythonObject callAttribute(String name, PythonObject... args) {
        PythonArgument argument = new PythonArgument();
        argument.addPositionalArgumentsFromArray(args);
        
        return callAttribute(name, argument);
    }

    public PythonObject callNestedAttribute(String name, PythonObject... args) {
        return Attributes.callNestedAttribute(this, name, args);
    }
    
    public PythonObject call(PythonObject... args) {
        return call(new PythonArgument().addPositionalArgumentsFromArray(args));
    }

    public PythonObject call(PythonParameter parameter) {
        return callTypeAttribute("__call__", parameter);
    }
    
    public boolean toBoolean() {
        PythonObject result = Builtins.bool.call(this);
        // FIXME: Validate return value
        
        return result == Builtins.False ? false : true;
    }
    
    public PythonObject getItem(PythonObject key) {
        return callAttribute("__getitem__", new PythonArgument()
            .addPositionalArgument(key));
    }

    public PythonObject getItem(long key) {
        return getItem(newInt(key));
    }

    public boolean getItemJB(long key) {
        return getItem(key).toBoolean();
    }

    public PythonObject sliceTo(long end) {
        return getItem(newSlice(None, newInt(end)));
    }
    
    public void setItem(PythonObject key, PythonObject value) {
        callAttribute("__setitem__", key, value);
    }
    
    public void raise() {
    }
    
    public int toInt() {
        throw new IllegalArgumentException("Cannot unpack non int object");
    }
    
    public int length() {
        return Builtins.len.call(this).toInt();
    }
    
    public boolean jin(PythonObject value) {
        return in(value).toBoolean();
    }
    
    public PythonObject in(PythonObject value) {
        return callTypeAttribute("__contains__", value);
    }
    
    public boolean jNotEquals(PythonObject other) {
        return notEquals(other).toBoolean();
    }


    public boolean jLessThan(PythonObject value) {
        return lessThan(value).toBoolean();
    }

    public boolean jGreaterThan(PythonObject value) {
        return greaterThan(value).toBoolean();
    }

    public PythonObject greaterThan(PythonObject value) {
        return callTypeAttribute("__gt__", value);
    }
    
    public PythonObject lessThan(PythonObject value) {
        return callTypeAttribute("__lt__", value);
    }

    public PythonObject add(PythonObject value) {
        return callTypeAttribute("__add__", value);
    }

    public PythonObject subtract(PythonObject value) {
        return callTypeAttribute("__sub__", value);
    }

    public PythonObject multiply(PythonObject value) {
        return callAttribute("__mul__", value);
    }

    public PythonObject getSuper() {
        return Builtins.super0.call(
            com.abiddarris.common.renpy.internal.core.Types.type(this),
            this);
    }

    public PythonObject pEquals(PythonObject other) {
        return callTypeAttribute("__eq__", other);
    }
    
    public PythonObject notEquals(PythonObject other) {
        return callTypeAttribute("__ne__", other);
    }

    public PythonObject notEquals(String other) {
        return notEquals(newString(other));
    }
    
    public ClassDefiner defineClass(String name, PythonObject... bases) {
        return new ClassDefiner(name, bases, getAttribute("__name__"), new ModuleTarget(this));
    }

    @Override
    public PythonObject defineAttribute(String name, PythonObject attribute) {
        setAttribute(name, attribute);

        return attribute;
    }

    @Override
    public PythonObject getModuleName() {
        if (jIsinstance(this, ModuleType)) {
            return getAttribute("__package__");
        }
        return getAttribute("__module__");
    }
    
    public PythonObject addNewAttribute(String name, PythonObject attribute) {
        setAttribute(name, attribute);
        
        return attribute;
    }
    
    public PythonObject addNewClass(String name, PythonObject... parents) {
        PythonObject class0 = newClass(name, newTuple(parents), newDict());
      
        setAttribute(name, class0);
        
        return class0;
    }
    
    public PythonObject addNewFunction(String name, Class sourceClass, String methodName, String... parameters) {
        PythonObject function = newFunction(sourceClass, methodName, parameters);
        setAttribute(name, function);
        
        return function;
    }

    public PythonObject addNewFunction(String name, PythonObject decorator, Class sourceClass, String methodName, String... parameters) {
        PythonObject function = newFunction(sourceClass, methodName, parameters);
        function = decorator.call(function);

        setAttribute(name, function);

        return function;
    }
    
    public PythonObject addNewFunction(String name, Class sourceClass, String methodName, PythonSignature signature) {
        PythonObject function = newFunction(sourceClass, methodName, signature);
        setAttribute(name, function);
        
        return function;
    }
    
    public PythonObject importModule(String name) {
        return importModule(newString(name));
    }
    
    public PythonObject importModule(PythonObject name) {
        PythonObject module = Builtins.__import__.call(name);
        String jName = name.toString();
        int period = jName.indexOf(".");
        
        jName = period != -1 ? jName.substring(0, period) : jName;
        
        setAttribute(jName, module);
        
        return module;
    }
    
    public PythonObject[] fromImport(String modName, String attributeName, String... attributeNames) {
        return importFrom(modName, new PythonObjectLoadTarget(this), attributeName, attributeNames);
    }
    
    public PythonObject callTypeAttribute(String name, PythonObject... args) {
        PythonArgument argument = new PythonArgument();
        argument.addPositionalArgumentsFromArray(args);
       
        return callTypeAttribute(name, argument);
    }
    
    private int getHashCode() {
        return super.hashCode();
    }
    
    private PythonObject callTypeAttribute(String name, PythonParameter parameter) {
        return getTypeAttribute(name).call(parameter);
    }
    
    PythonObject getTypeAttribute(String name) {
        PythonObject type = (PythonObject)attributes.get("__class__");
        PythonObject attribute = attributes.findAttributeWithoutTypeAllowConversion(type, name);
        if(attribute == null) {
            Builtins.AttributeError.call().raise();
        }
        
        return attribute;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PythonObject)) {
            return false;
        }
        
        PythonObject result = callTypeAttribute("__eq__", new PythonArgument()
            .addPositionalArgument((PythonObject)obj));
        return result.toBoolean();
    }
    
    @Override
    public int hashCode() {
        return unpackPythonInt(callTypeAttribute("__hash__", new PythonParameter()));
    }

    @Override
    public String toString() {
        return callTypeAttribute("__str__").toString();
    }
    
    @Override
    public Iterator<PythonObject> iterator() {
        PythonObject pythonIterator = callTypeAttribute("__iter__", new PythonArgument());
        
        return new IteratorWrapper(pythonIterator);
    }

    @Deprecated
    public static PythonObject getItem(PythonObject item, PythonObject key) {
        return item.getItem(key);
    }
    
    @Deprecated
    public static PythonObject newPythonString(String string) {
        return newString(string);
    }
    
    @Deprecated
    public static PythonObject newPythonInt(int value) {
        return newInt(value);
    }
    
    @Deprecated
    public static int unpackPythonInt(PythonObject integer) {
        if(!(integer instanceof PythonInt)) {
            throw new IllegalArgumentException("Cannot unpack non int object");
        }
        
        return ((PythonInt)integer).toInt();
    }
    
    private static PythonObject eq(PythonObject self, PythonObject obj) {
        return newBoolean(self == obj);
    }
    
    private static void objectInit(PythonObject self) {
    }
    
    private static PythonObject typeCall(PythonObject self, PythonObject args, PythonObject kwargs) {
        PythonObject newFunction = self.getAttribute("__new__");
        PythonArgument arguments = (PythonArgument) new PythonArgument()
             .addPositionalArgument(self);
        
        if(newFunction != Builtins.object.getAttribute("__new__")) {
            arguments.addPositionalArguments(args)
                .addKeywordArguments(kwargs);
        }
        PythonObject instance = newFunction.call(arguments);
        self.callAttribute("__init__", new PythonArgument()
            .addPositionalArgument(instance)
            .addPositionalArguments(args)
            .addKeywordArguments(kwargs));
        
        return instance;
    }

    static class PythonBoolean extends PythonInt {

        PythonBoolean(int val) {
            super(val);
            
            setAttributeDirectly("__class__", Builtins.bool);
        }
        
        private static PythonObject newBoolean(PythonObject cls, PythonObject obj) {
            if(obj instanceof PythonBoolean) {
                return obj;
            }
            
            return obj.callTypeAttribute("__bool__", new PythonParameter());
        }
        
        private static PythonObject toBoolean(PythonObject self) {
            return self;
        }
        
    }
    
    static class PythonBaseException extends PythonObject {
        
        private PythonObject args;
        
        private PythonBaseException(PythonObject cls, PythonObject args) {
            super(new BootstrapAttributeHolder());
            
            setAttributeDirectly("__class__", cls);
            
            int len = args.length();
            if(len == 1) {
                this.args = args.getItem(newInt(0));
            } else if(len == 0) {
                this.args = newString("");
            } else {
                this.args = args;
            }
        }
        
        private static PythonObject newException(PythonObject cls, PythonObject args) {
            return new PythonBaseException(cls, args);
        }

        private static void init(PythonObject self, PythonObject args) {
        }
        
        @Override
        public void raise() {
            throw new PythonException(this, getAttribute("__name__") + ": " + args.toString());
        }
    }
    
    private static class IteratorWrapper implements Iterator<PythonObject> {
        
        private PythonObject iterator;
        private PythonObject currentElement;
        private boolean eoi;
        
        private IteratorWrapper(PythonObject iterator) {
            this.iterator = iterator;
        }
        
        @Override
        public boolean hasNext() {
            nextInternal();
            
            return currentElement != null;
        }
        
        @Override
        public PythonObject next() {
            nextInternal();
            
            PythonObject element = currentElement;
            if(element == null) {
                throw new NoSuchElementException();
            }
            currentElement = null;
            
            return element;
        }
        
        private void nextInternal() {
            if(currentElement != null || eoi) {
                return;
            }
            tryExcept(() -> {
                currentElement = iterator.callAttribute("__next__", new PythonArgument());
            })
            .onExcept((e) -> {
                eoi = true;
            }, Builtins.StopIteration)
            .execute();
        }
        
    }
}
