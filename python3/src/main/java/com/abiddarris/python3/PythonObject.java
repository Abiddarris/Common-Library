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
package com.abiddarris.python3;

import static com.abiddarris.python3.Builtins.None;
import static com.abiddarris.python3.Types.ModuleType;
import static com.abiddarris.python3.core.Errors.raiseAttributeError;
import static com.abiddarris.python3.core.JFunctions.jIsinstance;
import static com.abiddarris.python3.core.Slice.newSlice;
import static com.abiddarris.python3.imp.Imports.importFrom;
import static java.util.Arrays.copyOfRange;

import com.abiddarris.python3.attributes.AttributeHolder;
import com.abiddarris.python3.attributes.AttributeManager;
import com.abiddarris.python3.attributes.BootstrapAttributeHolder;
import com.abiddarris.python3.attributes.PythonAttributeHolder;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.builder.DecorateAttribute;
import com.abiddarris.python3.builder.ModuleTarget;
import com.abiddarris.python3.core.Attributes;
import com.abiddarris.python3.core.Objects;
import com.abiddarris.python3.defineable.Defineable;
import com.abiddarris.python3.imp.ImportAsTarget;
import com.abiddarris.python3.imp.PythonObjectLoadTarget;
import com.abiddarris.python3.signature.PythonArgument;
import com.abiddarris.python3.signature.PythonParameter;
import com.abiddarris.python3.signature.PythonSignature;
import com.abiddarris.common.utils.ObjectWrapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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

        if (cls != null) {
            setAttributeDirectly("__class__", cls);
        }
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

    public void setAttribute(String name, PythonObject obj) {
        setAttribute(newString(name), obj);
    }

    public void setAttribute(PythonObject name, PythonObject value) {
        if (attributes.get("__class__").attributes.isOptimizeSetter()) {
            Objects.setAttribute(this, name, value);
            return;
        }
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
        if (attributes.get("__class__").attributes.isOptimizeGetter()) {
            return typeGetAttribute(this, newString(name));
        }
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

    public PythonObject getNestedAttribute(String name) {
        return Attributes.getNestedAttribute(this, name);
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

    public PythonObject getItemAttribute(long key, String name) {
        return getItem(key).getAttribute(name);
    }

    public boolean getItemJB(long key) {
        return getItem(key).toBoolean();
    }

    public boolean getNestedAttributeJB(String name) {
        return getNestedAttribute(name).toBoolean();
    }

    public PythonObject getAttributeItem(String name, PythonObject key) {
        return getAttribute(name).getItem(key);
    }

    public PythonObject getAttributeItem(String name, long key) {
        return getAttributeItem(name, newInt(key));
    }

    public PythonObject getAttributeItem(String name, String key) {
        return getAttributeItem(name, newString(key));
    }

    public PythonObject sliceTo(long end) {
        return getItem(newSlice(None, newInt(end)));
    }

    public PythonObject sliceFrom(long start) {
        return getItem(newSlice(newInt(start), None));
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

    public boolean jin(String value) {
        return jin(newString(value));
    }

    public PythonObject in(PythonObject value) {
        return callTypeAttribute("__contains__", value);
    }

    public boolean jNotEquals(PythonObject other) {
        return notEquals(other).toBoolean();
    }

    public boolean jNotEquals(long other) {
        return notEquals(other).toBoolean();
    }

    public boolean jNotEquals(String other) {
        return notEquals(newString(other)).toBoolean();
    }

    public boolean jLessThan(PythonObject value) {
        return lessThan(value).toBoolean();
    }

    public boolean jLessThan(int value) {
        return jLessThan(newInt(value));
    }

    public boolean jLessEquals(PythonObject value) {
        return lessEquals(value).toBoolean();
    }

    public boolean jLessEquals(long value) {
        return jLessEquals(newInt(value));
    }

    public boolean jGreaterThan(PythonObject value) {
        return greaterThan(value).toBoolean();
    }

    public boolean jGreaterThan(long value) {
        return jGreaterThan(newInt(value));
    }

    public boolean jGreaterEquals(PythonObject value) {
        return greaterEquals(value).toBoolean();
    }

    public PythonObject greaterEquals(PythonObject value) {
        return callTypeAttribute("__ge__", value);
    }

    public PythonObject greaterThan(PythonObject value) {
        return callTypeAttribute("__gt__", value);
    }

    public PythonObject greaterThan(int i) {
        return greaterThan(newInt(i));
    }

    public PythonObject lessEquals(PythonObject value) {
        return callTypeAttribute("__le__", value);
    }

    public PythonObject lessThan(PythonObject value) {
        return callTypeAttribute("__lt__", value);
    }

    public PythonObject add(long value) {
        return add(newInt(value));
    }

    public PythonObject add(String value) {
        return add(newString(value));
    }

    public PythonObject add(PythonObject value) {
        return callTypeAttribute("__add__", value);
    }

    public PythonObject subtract(PythonObject value) {
        return callTypeAttribute("__sub__", value);
    }

    public PythonObject subtract(int value) {
        return subtract(newInt(value));
    }

    public PythonObject multiply(PythonObject value) {
        return callAttribute("__mul__", value);
    }

    public PythonObject multiply(long value) {
        return multiply(newInt(value));
    }

    public PythonObject getSuper() {
        return Builtins.super0.call(
            com.abiddarris.python3.core.Types.type(this),
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

    public PythonObject notEquals(long other) {
        return notEquals(newInt(other));
    }

    public ClassDefiner defineClass(String name, PythonObject... bases) {
        return new ClassDefiner(name, bases, getAttribute("__name__"), new ModuleTarget(this));
    }

    public ClassDefiner defineDecoratedClass(String name, PythonObject decorator, PythonObject... bases) {
        return new ClassDefiner(name, bases, getAttribute("__name__"),
                new DecorateAttribute(new ModuleTarget(this), decorator));
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

    public PythonObject[] fromImportAs(String modName, String... nameAndAs) {
        if (nameAndAs.length == 0) {
            return new PythonObject[0];
        }
        if (nameAndAs.length % 2 == 1) {
            throw new IllegalStateException("1 attribute does not have an alias");
        }

        Map<String, String> imports = new LinkedHashMap<>();
        for (int i = 0; i < nameAndAs.length; i += 2) {
            imports.put(nameAndAs[i], nameAndAs[i + 1]);
        }

        String[] attributes = imports.keySet().toArray(new String[0]);
        return importFrom(modName, new ImportAsTarget(new PythonObjectLoadTarget(this), imports),
                nameAndAs[0], copyOfRange(attributes, 1, attributes.length));
    }

    public PythonObject callTypeAttribute(String name, PythonObject... args) {
        PythonArgument argument = new PythonArgument();
        argument.addPositionalArgumentsFromArray(args);

        return callTypeAttribute(name, argument);
    }

    public boolean callAttributeJB(String name, PythonObject... args) {
        return callAttribute(name, args).toBoolean();
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
            raiseAttributeError(this, newString(name));
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

    public boolean equals(String str) {
        return equals(newString(str));
    }

    public boolean equals(long l) {
        return equals(newInt(l));
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
