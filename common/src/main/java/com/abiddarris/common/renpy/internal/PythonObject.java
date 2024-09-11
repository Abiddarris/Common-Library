package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.core.Errors.raiseAttributeError;
import static com.abiddarris.common.renpy.internal.imp.Imports.importFrom;

import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.builder.ModuleTarget;
import com.abiddarris.common.renpy.internal.core.Enumerate;
import com.abiddarris.common.renpy.internal.core.Functions;
import com.abiddarris.common.renpy.internal.core.Objects;
import com.abiddarris.common.renpy.internal.core.Super;
import com.abiddarris.common.renpy.internal.core.classes.Classes;
import com.abiddarris.common.renpy.internal.core.classes.DelegateType;
import com.abiddarris.common.renpy.internal.imp.PythonObjectLoadTarget;
import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;
import com.abiddarris.common.renpy.internal.model.AttributeHolder;
import com.abiddarris.common.renpy.internal.model.AttributeManager;
import com.abiddarris.common.renpy.internal.model.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.model.PythonAttributeHolder;
import com.abiddarris.common.renpy.internal.object.PythonMethod;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;
import com.abiddarris.common.utils.ObjectWrapper;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PythonObject extends Python implements Iterable<PythonObject> {

    private static final PythonObject method;
    private static final PythonObject NoneType;
    
    public static final PythonObject object;
    public static final PythonObject type;
    public static final PythonObject function;
    public static final PythonObject str;
    public static final PythonObject tuple;
    public static final PythonObject int0;
    public static final PythonObject dict;
    public static final PythonObject bool;
    public static final PythonObject False;
    public static final PythonObject True;
    public static final PythonObject Exception;
    public static final PythonObject StopIteration;
    public static final PythonObject AttributeError;
    public static final PythonObject TypeError;
    public static final PythonObject None;
    public static final PythonObject list;
    public static final PythonObject IndexError;
    public static final PythonObject ModuleNotFoundError;
    public static final PythonObject KeyError;
    public static final PythonObject ValueError;
    public static final PythonObject super0;
    public static final PythonObject RuntimeError;
    public static final PythonObject NotImplementedError;
    public static final PythonObject enumerate;
    
    public static final PythonObject builtins;
    
    public static final PythonObject len;
    public static final PythonObject issubclass;
    public static final PythonObject __import__;
    public static final PythonObject isinstance;
    public static final PythonObject hasattr;
    
    static {
        type = newBootstrapObject();
        object = newBootstrapObject();
        tuple = newBootstrapObject();
        function = newBootstrapObject();

        PythonObject defaultBases = newTuple(object);

        object.setAttributeDirectly("__class__", type);
        object.setAttributeDirectly("__setattr__", newFunction(Objects.class, "setAttribute", "self", "name", "value"));
        object.setAttributeDirectly("__getattribute__", newFunction(PythonObject.class, "typeGetAttribute", "self","name"));

        type.setAttributeDirectly("__class__", type);
        type.setAttributeDirectly("__bases__", defaultBases);
        type.setAttributeDirectly("__mro__", newTuple(type, object));

        str = newBootstrapObject();
        str.setAttributeDirectly("__class__", type);
        str.setAttribute("__bases__", defaultBases);
        str.setAttribute("__name__", newPythonString("str"));
        str.setAttribute("__hash__", newFunction(findMethod(PythonString.class, "stringHash"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()));
        str.setAttribute("__eq__", newFunction(
            findMethod(PythonString.class, "stringEq"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .addParameter("obj")
                .build()
        ));
        str.setAttribute("__new__", newFunction(findMethod(BuiltinsImpl.class, "strNew"), "self", "obj"));
        str.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "strInit"), "self", "obj"));
        str.setAttribute("__mro__", newTuple(str, object));
        str.setAttribute("__contains__", newFunction(PythonString.class, "contains", "self", "key"));
        
        int0 = newBootstrapObject();
        int0.setAttributeDirectly("__class__", type);
        int0.setAttribute("__bases__", defaultBases);
        int0.setAttribute("__name__", newPythonString("int"));
        int0.setAttribute("__hash__", newFunction(PythonInt.class, "hash", "self"));
        int0.setAttribute("__eq__", newFunction(PythonInt.class, "eq", "self", "other"));
        int0.setAttribute("__mro__", newTuple(int0, object));

        function.setAttributeDirectly("__class__", type);
        function.setAttribute("__bases__", defaultBases);
        function.setAttribute("__name__", newPythonString("function"));
        function.setAttribute("__mro__", newTuple(function, object));

        len = newFunction(findMethod(BuiltinsImpl.class, "len"), "obj");

        object.setAttribute("__name__", newPythonString("object"));
        object.setAttribute("__new__", newFunction(
            findMethod(PythonObject.class, "pythonObjectNew"),
            new PythonSignatureBuilder()
                .addParameter("cls")
                .build()
        ));
        object.setAttribute("__hash__",
            newFunction(
                findMethod(PythonObject.class, "objectHash"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .build()
            )
        );
        object.setAttribute("__eq__", newFunction(
            findMethod(PythonObject.class, "eq"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .addParameter("obj")
                .build()
        ));
        object.setAttribute("__init__", newFunction(
            findMethod(PythonObject.class, "objectInit"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()
        ));
        object.setAttribute("__str__", newFunction(findMethod(BuiltinsImpl.class, "objectStr"), "self"));
        object.setAttribute("__bases__", newTuple());
        object.setAttribute("__ne__", newFunction(BuiltinsImpl.class, "objectNe", "self", "other"));
        object.setAttribute("__mro__", newTuple(object));

        tuple.setAttributeDirectly("__class__", type);
        tuple.setAttribute("__bases__", defaultBases);
        tuple.setAttribute("__name__", newPythonString("tuple"));
        tuple.setAttribute("__getitem__", newFunction(
            findMethod(PythonTuple.class, "getitem"),
            new PythonSignatureBuilder()  
                .addParameter("self")    
                .addParameter("index")
                .build()
        ));
        tuple.setAttribute("__len__", newFunction(
            findMethod(PythonTuple.class, "len"),
            new PythonSignatureBuilder()   
                .addParameter("self") 
                .build()
        ));
        tuple.setAttribute("__iter__", newFunction(
            findMethod(PythonTuple.class, "iter"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()
        ));
        tuple.setAttribute("__str__", newFunction(PythonTuple.class, "str", "self"));
        tuple.setAttribute("__mro__", newTuple(tuple, object));

        dict = newBootstrapObject();
        dict.setAttributeDirectly("__class__", type);
        dict.setAttribute("__bases__", defaultBases);
        dict.setAttribute("__name__", newPythonString("dict"));
        dict.setAttribute("__getitem__", newFunction(
            findMethod(PythonDict.class, "dictGetItem"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .addParameter("key")
                .build()
        ));
        dict.setAttribute("__iter__", newFunction(
            findMethod(PythonDict.class, "iter"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()
        ));
        dict.setAttribute("__setitem__", newFunction(findMethod(PythonDict.class, "setItem"), "self", "key", "value"));
        dict.setAttribute("__contains__", newFunction(PythonDict.class, "contains", "self", "value"));
        dict.setAttribute("__len__", newFunction(PythonDict.class, "len", "self"));
        dict.setAttribute("__str__", newFunction(PythonDict.class, "str", "self"));
        dict.setAttribute("update", newFunction(PythonDict.class, "update", "self", "other"));
        dict.setAttribute("__new__", newFunction(PythonDict.class, "new0", "cls"));
        dict.setAttribute("__mro__", newTuple(dict, object));
        type.setAttribute("__name__", newPythonString("type"));

        type.setAttribute("__new__", newFunction(
            findMethod(Classes.class, "typeNew"), "cls", "*args"));
        type.setAttribute("__getattribute__",
            newFunction(
                findMethod(PythonObject.class, "typeGetAttribute"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .addParameter("name")
                    .build()
            )
        );
        type.setAttribute("__init__", newFunction(
            findMethod(Classes.class, "typeInit"), "cls", "*args"
        ));
        type.setAttribute("__call__", newFunction(
            findMethod(PythonObject.class, "typeCall"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .addParameter("*args")
                .addParameter("**kwargs")
                .build()
        ));
        type.setAttribute("__subclasscheck__", newFunction(findMethod(BuiltinsImpl.class, "typeSubclassCheck"), "self", "other"));
        type.setAttribute("__str__", newFunction(findMethod(BuiltinsImpl.class, "typeStr"), "self"));
        type.setAttribute("__instancecheck__", newFunction(BuiltinsImpl.class, "objectInstanceCheck", "self", "other"));

        method = Bootstrap.newClass(type, newTuple(newPythonString("method"), newTuple(object)));
       
        Exception = Bootstrap.newClass(type, newTuple(newString("exception"), newTuple()), new BootstrapAttributeHolder());
        Exception.setAttribute("__new__", newFunction(
            findMethod(PythonBaseException.class, "newException"),
            new PythonSignatureBuilder() 
                .addParameter("cls")
                .addParameter("*args")  
                .build() 
        ));
        Exception.setAttribute("__init__", newFunction(findMethod(PythonBaseException.class, "init"), "self", "*args"));
        
        StopIteration = Bootstrap.newClass(type, newTuple(newString("StopIteration"), newTuple(Exception)), new BootstrapAttributeHolder());
        KeyError = Bootstrap.newClass(type, newTuple(newString("KeyError"), newTuple(Exception)), new BootstrapAttributeHolder());
        
        PythonDict.init();
        
        bool = Bootstrap.newClass(type, newTuple(newString("bool"), newTuple(int0)), new BootstrapAttributeHolder());
        bool.setAttribute("__new__", newFunction(findMethod(BuiltinsImpl.class, "boolNew"), 
                "cls", "obj"));
        bool.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "boolInit"), 
                "cls", "obj"));
        bool.setAttribute("__bool__", newFunction(
                findMethod(PythonBoolean.class, "toBoolean"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .build()));
        
        False = new PythonBoolean(0);
        True = new PythonBoolean(1);
        
        PythonTuple.init();

        TypeError = type.call(newString("TypeError"), newTuple(Exception), newDict());
        AttributeError = type.call(newString("AttributeError"), newTuple(Exception), newDict());

        DelegateType.activate();

        issubclass = newFunction(findMethod(Functions.class, "issubclass"), "cls", "base");
        
        NoneType = type.call(newString("NoneType"), newTuple(type), newDict());
        None = NoneType.call(newString("None"), newTuple(), newDict());
        
        NoneType.setAttribute("__new__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeNew"), "cls"));
        NoneType.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeInit"), "cls"));
        NoneType.setAttribute("__call__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeCall"), "self"));
        NoneType.setAttribute("__bool__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeBool"), "self"));
        
        str.setAttribute("rsplit", newFunction(PythonString.class, "rsplit", 
                new PythonSignatureBuilder("self")
                    .addParameter("sep", None)
                    .addParameter("maxsplit", newInt(-1))
                    .build()));
        
        Types.init();
        
        builtins = createModule("builtins");
        
        PythonList.init();
        
        __import__ = newFunction(findMethod(BuiltinsImpl.class, "importImpl"), "name");
        list = newClass("list", newTuple(), newDict(
                newString("__getitem__"), newFunction(findMethod(PythonList.class, "getItem"), "self", "index"),
                newString("insert"), newFunction(findMethod(PythonList.class, "insert"), "self", "index", "element"),
                newString("__iter__"), newFunction(findMethod(PythonList.class, "iter"), "self"),
                newString("append"), newFunction(PythonList.class, "append", "self", "append"),
                newString("pop"), newFunction(PythonList.class, "pop", "self"),
                newString("__len__"), newFunction(PythonList.class, "len", "self"),
                newString("__setitem__"), newFunction(findMethod(PythonList.class, "setItem"), "self", "key", "value")
        ));
        
        Sys.init();
        
        IndexError = newClass("IndexError", newTuple(Exception), newDict());
        ModuleNotFoundError = newClass("ModuleNotFoundError", newTuple(Exception), newDict());
        
        JavaModuleLoader.init();
        
        isinstance = newFunction(Functions.class, "isInstance", "instance", "class");
        hasattr = newFunction(BuiltinsImpl.class, "hasAttr", "obj", "name");
        
        ValueError = builtins.defineClass("ValueError", Exception).define();
        
        super0 = Super.define(builtins);
        
        RuntimeError = builtins.defineClass("RuntimeError", Exception).define();
        NotImplementedError = builtins.defineClass("NotImplementedError", RuntimeError).define();

        enumerate = Enumerate.define(builtins);

        /*object.addMethod(
                "__setattr__",
                (args, kwargs) -> {
                    object.setAttribute((String) args.get(1), args.get(2));

                    return null;
                });*/
    }
    
    private static PythonObject newBootstrapObject() {
        return new PythonObject(new BootstrapAttributeHolder());
    }
    
    private static PythonObject pythonObjectNew(PythonObject cls) {
        PythonObject instance = new PythonObject();
        instance.setAttributeDirectly("__class__", cls);

        return instance;
    }
    
    private static PythonObject objectHash(PythonObject self) {
        return newPythonInt(self.getHashCode());
    }
    
    private static PythonObject typeGetAttribute(PythonObject self, PythonObject name) {
        PythonObject attribute = self.attributes.findAttribute(name.toString());
        if(attribute == null) {
            raiseAttributeError(self, name);
        }
        
        return attribute;
    }
    
    AttributeManager attributes;
    
    public PythonObject(AttributeHolder holder) {
        if(holder == null) {
            holder = new PythonAttributeHolder();
        } 
        
        attributes = new AttributeManager(this, holder);
    }
    
    public PythonObject() {
        this(null);
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

    public PythonObject getAttribute(String name) {
        PythonObject attribute = callTypeAttribute("__getattribute__",
            new PythonParameter()
                .addPositionalArgument(newPythonString(name)));
        
        return attribute;
    }
    
    public PythonObject getAttribute(String name, PythonObject defaultValue) {
        ObjectWrapper<PythonObject> returnValue = new ObjectWrapper<>(defaultValue);
        tryExcept(() -> returnValue.setObject(getAttribute(name))).
        onExcept((e) -> {}, AttributeError).execute();
        
        return returnValue.getObject();
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
    
    public PythonObject call(PythonObject... args) {
        return call(new PythonArgument().addPositionalArgumentsFromArray(args));
    }

    public PythonObject call(PythonParameter parameter) {
        return callTypeAttribute("__call__", parameter);
    }
    
    public boolean toBoolean() {
        PythonObject result = bool.call(this);
        // FIXME: Validate return value
        
        return result == False ? false : true;
    }
    
    public PythonObject getItem(PythonObject key) {
        return callAttribute("__getitem__", new PythonArgument()
            .addPositionalArgument(key));
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
        return len.call(this).toInt();
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
    
    public PythonObject getSuper() {
        return super0.call(
            com.abiddarris.common.renpy.internal.core.Types.type(this),
            this);
    }
    
    public PythonObject notEquals(PythonObject other) {
        return callTypeAttribute("__ne__", other);
    }
    
    public ClassDefiner defineClass(String name, PythonObject... bases) {
        return new ClassDefiner(name, bases, getAttribute("__name__"), new ModuleTarget(this));
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
    
    public PythonObject addNewFunction(String name, Class sourceClass, String methodName, PythonSignature signature) {
        PythonObject function = newFunction(sourceClass, methodName, signature);
        setAttribute(name, function);
        
        return function;
    }
    
    public PythonObject importModule(String name) {
        return importModule(newString(name));
    }
    
    public PythonObject importModule(PythonObject name) {
        PythonObject module = __import__.call(name);
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
            AttributeError.call().raise();
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
        
        if(newFunction != object.getAttribute("__new__")) {
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

    private static class PythonBoolean extends PythonInt {
        
        private PythonBoolean(int val) {
            super(val);
            
            setAttributeDirectly("__class__", bool);
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
    
    private static class PythonBaseException extends PythonObject {
        
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
            }, StopIteration)
            .execute();
        }
        
    }
}
