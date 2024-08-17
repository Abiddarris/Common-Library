package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonSyntax.getAttr;

import static java.util.Collections.emptyMap;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;
import com.abiddarris.common.renpy.internal.trycatch.ExceptFinally;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

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
    
    public static final PythonObject len;
    public static final PythonObject issubclass;
    public static final PythonObject __import__;
    
    static {
        type = new PythonObject();
        object = new PythonObject();
        tuple = new PythonObject();
        function = new PythonObject();
        
        PythonObject defaultBases = newTuple(object);

        str = new PythonObject();
        str.setAttribute("__bases__", defaultBases);
        str.setAttribute("__name__", newPythonString("str"));
        str.setAttribute("__class__", type);
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
        
        int0 = new PythonObject();
        int0.setAttribute("__bases__", defaultBases);
        int0.setAttribute("__class__", type);
        int0.addField("__name__", newPythonString("int"));

        function.setAttribute("__bases__", defaultBases);
        function.setAttribute("__class__", type);
        function.addField("__name__", newPythonString("function"));

        len = newFunction(findMethod(BuiltinsImpl.class, "len"), "obj");
        
        object.setAttribute("__class__", type);
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
        object.setAttribute("__getattribute__", 
            newFunction(
                findMethod(PythonObject.class, "typeGetAttribute"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .addParameter("name")
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
        
        tuple.setAttribute("__class__", type);
        tuple.setAttribute("__bases__", defaultBases);
        tuple.addField("__name__", newPythonString("tuple"));
        tuple.addField("__getitem__", newFunction(
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
        
        dict = new PythonObject();
        dict.setAttribute("__bases__", defaultBases);
        dict.setAttribute("__class__", type);
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
        
        type.setAttribute("__bases__", defaultBases);
        type.setAttribute("__class__", type);
        type.setAttribute("__name__", newPythonString("type"));
        type.setAttribute("__new__", newFunction(
            findMethod(BuiltinsImpl.class, "typeNew"), "cls", "*args"));               
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
            findMethod(BuiltinsImpl.class, "typeInit"), "cls", "*args"
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
        
        method = Bootstrap.newClass(type, newTuple(newPythonString("method"), newTuple(object)));
       
        Exception = Bootstrap.newClass(type, newTuple(newString("exception"), newTuple()));
        Exception.setAttribute("__new__", newFunction(
            findMethod(PythonBaseException.class, "newException"),
            new PythonSignatureBuilder() 
                .addParameter("cls")
                .addParameter("*args")  
                .build() 
        ));
        Exception.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "typeInit"), "self", "*args"));
        
        StopIteration = Bootstrap.newClass(type, newTuple(newString("StopIteration"), newTuple(Exception)));
        
        PythonDict.init();
        
        bool = type.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(type)
            .addPositionalArgument(newString("bool"))
            .addPositionalArgument(newTuple(int0))
            .addPositionalArgument(newDict(emptyMap())));
        bool.setAttribute("__new__", newFunction(findMethod(BuiltinsImpl.class, "boolNew"), 
                "cls", "obj"));
        bool.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "boolInit"), 
                "cls", "obj"));
        bool.setAttribute("__bool__", newFunction(
                findMethod(PythonBoolean.class, "toBoolean"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .build()));
        
        False = new PythonBoolean();
        True = new PythonBoolean();
        
        PythonTuple.init();
        
        TypeError = type.call(newString("TypeError"), newTuple(Exception), newDict());
        AttributeError = type.call(newString("AttributeError"), newTuple(Exception), newDict());
        
        issubclass = newFunction(findMethod(BuiltinsImpl.class, "isSubclass"), "cls", "base");
        
        NoneType = type.call(newString("NoneType"), newTuple(type), newDict());
        None = NoneType.call(newString("None"), newTuple(), newDict());
        
        NoneType.setAttribute("__new__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeNew"), "cls"));
        NoneType.setAttribute("__init__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeInit"), "cls"));
        NoneType.setAttribute("__call__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeCall"), "self"));
        NoneType.setAttribute("__bool__", newFunction(findMethod(BuiltinsImpl.class, "noneTypeBool"), "self"));
        
        Types.init();
        Sys.init();
        
        __import__ = newFunction(findMethod(BuiltinsImpl.class, "import0"), "name");
        list = newClass("list", newTuple(), newDict(
            newString("__getitem__"), newFunction(findMethod(PythonList.class, "getItem"), "self", "index")
        ));
        IndexError = newClass("IndexError", newTuple(Exception), newDict());
        /*object.addMethod(
                "__setattr__",
                (args, kwargs) -> {
                    object.setAttribute((String) args.get(1), args.get(2));

                    return null;
                });*/
    }
    
    private static PythonObject pythonObjectNew(PythonObject cls) {
        PythonObject instance = new PythonObject();
        instance.setAttribute("__class__", cls);
        
        return instance;
    }
    
    private static PythonObject objectHash(PythonObject self) {
        return newPythonInt(self.getHashCode());
    }
    
    public static PythonObject typeGetAttribute(PythonObject self, PythonObject name) {
        PythonObject attribute = findAttribute(self, name.toString());
        if(attribute == null) {
            AttributeError.call().raise();
        }
        
        return attribute;
    }
    
    private static PythonObject findAttribute(PythonObject self, String name) {
        PythonObject attribute = findAttributeWithoutType(self, name);
        if(attribute != null) {
            return attribute;
        }
        
        PythonObject type = (PythonObject)self.attributes.get("__class__");
        
        return findAttributeWithoutTypeAllowConversion(self, type, name);
    }
    
    private static PythonObject findAttributeWithoutTypeAllowConversion(PythonObject self, PythonObject type, String name) {
        PythonObject attribute = findAttributeWithoutType(type, name);
        if(attribute instanceof PythonFunction) {
            return new PythonMethod(self, attribute);
        }
        
        return attribute;
    }
    
    private static PythonObject findAttributeWithoutType(PythonObject self, String name) {
        PythonObject attribute = (PythonObject)self.attributes.get(name);
        if(attribute != null) {
            return attribute;
        }
        
        PythonTuple bases = (PythonTuple)self.attributes.get("__bases__");
        if(bases != null) {
            for(var element : bases.elements) {
                attribute = findAttributeWithoutType(element, name);
                if(attribute != null) {
                    return attribute;
                }
            }
        }
        
        return null;
    }
    
    protected Map<String, Object> attributes = new HashMap<>();
    
    public void addMethod(String name, PythonMethod func) {
        setAttribute(name, func);
    }

    private void addField(String name, Object obj) {
        setAttribute(name, obj);
    }

    public void setAttribute(String name, Object obj) {
        attributes.put(name, obj);
    }

    public PythonObject getAttribute(String name) {
        PythonObject attribute = callTypeAttribute("__getattribute__",
            new PythonParameter()
                .addPositionalArgument(newPythonString(name)));
        
        return attribute;
    }

    public boolean hasAttribute(String name) {
        if (attributes.containsKey(name)) return true;

        List<PythonObject> classes = (List<PythonObject>) attributes.get("__bases__");
        if (classes.isEmpty()) {
            classes = List.of(object);
        }

        for (var class0 : classes) {
            if (class0.hasAttribute(name)) return true;
        }

        return false;
    }

    public PythonObject invokeStaticMethod(String name, List args, Map kwargs) {
        PythonMethod method = (PythonMethod) getAttr(this, name, null);
        if (method == null) {
            throw new NoSuchMethodError(String.format("Method %s not found", name));
        }

        return method.call(args, kwargs);
    }

    public PythonObject call(List args, Map kwargs) {
        List args2 = new ArrayList<>();
        args2.add(this);
        args2.addAll(args);

        return invokeStaticMethod("__call__", args2, kwargs);
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
    
    public void raise() {
    }
    
    public int toInt() {
        throw new IllegalArgumentException("Cannot unpack non int object");
    }
    
    public int length() {
        return len.call(this).toInt();
    }
    
    PythonObject callTypeAttribute(String name, PythonObject... args) {
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
        PythonObject attribute = findAttributeWithoutTypeAllowConversion(this, type, name);
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
    
    public static PythonObject newFunction(Method javaMethod, String... argumentNames) {
        PythonSignatureBuilder builder = new PythonSignatureBuilder();
        for(String argumentName : argumentNames) {
        	builder.addParameter(argumentName);
        }
        return newFunction(javaMethod, builder.build());
    }
    
    public static PythonObject newFunction(Method javaMethod, PythonSignature signature) {
        return new PythonFunction(javaMethod, signature);
    }

    public static PythonObject newTuple(PythonObject... elements) {
        PythonObject object = new PythonTuple(elements);
        object.setAttribute("__class__", tuple);

        return object;
    }
    
    public static PythonObject newDict(Map<PythonObject, PythonObject> map) {
        PythonObject dict = new PythonDict(map);
        dict.setAttribute("__class__", PythonObject.dict);
        
        return dict;
    }
    
    public static PythonObject newDict(PythonObject... objects) {
        if(objects.length % 2 != 0) {
            throw new IllegalArgumentException("Missing value for " + objects[objects.length - 1]);
        }
        
        Map<PythonObject, PythonObject> map = new LinkedHashMap<>();
        for(int i = 0; i < objects.length; i += 2) {
        	map.put(objects[i], objects[i + 1]);
        }
        
        return newDict(map);
    }
    
    public static PythonObject newClass(String name, PythonObject bases, PythonObject attributes) {
        return newClass(null, name, bases, attributes);
    }
    
    public static PythonObject newClass(PythonObject _type, String name, PythonObject bases, PythonObject attributes) {
        if(_type == null) {
            _type = findType(bases);
        }
        
        return _type.call(newString(name), bases, attributes);
    }
    
    public static ExceptFinally tryExcept(Runnable tryRunnable) {
        return new ExceptFinally(tryRunnable);
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
    
    private static PythonObject findType(PythonObject bases) {
        if(!bases.toBoolean()) {
            return type;
        }
        
        PythonObject _type = type.call(bases.getItem(newInt(0)));
        int length = bases.length();
        if(length == 1) {
            return _type;
        }
        for(int i = 1; i < length; ++i) {
        	PythonObject meta = type.call(bases.getItem(newInt(i)));
            if(issubclass.call(meta, type).toBoolean()) {
                _type = meta;
            } else if(issubclass.call(_type, meta).toBoolean()) {
                TypeError.call().raise();
            }
        }
        
        return _type;
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
    
    static Method findMethod(Class source, String name) {
        Method[] methods = Stream.of(source.getDeclaredMethods())
            .filter(method -> method.getName().equals(name))
            .toArray(Method[]::new);
        
        if(methods.length > 1) {
            throw new IllegalArgumentException(methods.length + " found");
        }
        
        if(methods.length == 0) {
            throw new IllegalArgumentException("Not found");
        }
        
        methods[0].setAccessible(true);
        return methods[0];
    }

    private static class PythonTuple extends PythonObject {

        private static PythonObject tuple_iterator;
        
        private static void init() {
            tuple_iterator = type.callAttribute("__new__", new PythonArgument()
                .addPositionalArgument(type)
                .addPositionalArgument(newString("tuple_iterator"))
                .addPositionalArgument(newTuple(object))
                .addPositionalArgument(newDict(emptyMap())));
            tuple_iterator.setAttribute("__next__", newFunction(
                findMethod(TupleIterator.class, "next"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .build()
            ));
        }
        
        private PythonObject[] elements;

        public PythonTuple(PythonObject[] elements) {
            this.elements = elements;
        }
        
        private static PythonObject getitem(PythonObject self, PythonObject pos) {
            if(!(self instanceof PythonTuple)) {
                throw new IllegalArgumentException("Error");
            }
            
            return ((PythonTuple)self).elements[unpackPythonInt(pos)];
        }
        
        private static PythonObject iter(PythonTuple self) {
            TupleIterator iterator = new TupleIterator(self);
            iterator.setAttribute("__class__", tuple_iterator);
            
            return iterator;
        }
        
        private static PythonObject len(PythonTuple self) {
            return newPythonInt(self.elements.length);
        }
        
        private static class TupleIterator extends PythonObject {
            
            private int index;
            private PythonTuple tuple;
            
            private TupleIterator(PythonTuple tuple) {
                this.tuple = tuple;
            }
            
            private static PythonObject next(TupleIterator self) {
                PythonObject[] elements = self.tuple.elements;
                if(elements.length == self.index) {
                    StopIteration.callAttribute("__new__", new PythonArgument()
                        .addPositionalArgument(StopIteration))
                        .raise();
                }
                
                return elements[self.index++];
            }
        }
    }

    private static class PythonFunction extends PythonObject {

        private Method method;
        private PythonSignature signature;

        public PythonFunction(Method method, PythonSignature signature) {
            this.signature = signature;

            setAttribute("__class__", function);

            this.method = method;
        }

        @Override
        public PythonObject call(PythonParameter parameter) {
            return signature.invoke(method, parameter);
        }

        @Override
        public PythonObject call(List args, Map kwargs) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static class PythonMethod extends PythonObject {
        
        private PythonObject function;
        private PythonObject self;
        
        public PythonMethod(PythonObject self, PythonObject function) {
            this.self = self;
            this.function = function;
        }
        
        @Override
        public PythonObject call(PythonParameter parameter) {
            PythonParameter newParams = new PythonParameter(parameter);
            newParams.insertPositionalArgument(0, self);
            
            return function.call(newParams);
        }
        
    }
    
    private static class PythonBoolean extends PythonObject {
        
        private PythonBoolean() {
            setAttribute("__class__", bool);
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
            setAttribute("__class__", cls);
            
            if(args.length() == 1) {
                this.args = args.getItem(newInt(0));
            } else {
                this.args = args;
            }
        }
        
        private static PythonObject newException(PythonObject cls, PythonObject args) {
            return new PythonBaseException(cls, args);
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
