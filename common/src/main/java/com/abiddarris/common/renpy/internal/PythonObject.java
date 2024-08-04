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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

public class PythonObject implements Iterable<PythonObject> {

    private static final PythonObject method;
    
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
        
        int0 = new PythonObject();
        int0.setAttribute("__bases__", defaultBases);
        int0.setAttribute("__class__", type);
        int0.addField("__name__", newPythonString("int"));

        function.setAttribute("__bases__", defaultBases);
        function.setAttribute("__class__", type);
        function.addField("__name__", newPythonString("function"));

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
        
        type.setAttribute("__bases__", defaultBases);
        type.setAttribute("__class__", type);
        type.setAttribute("__name__", newPythonString("type"));
        type.setAttribute("__new__", newFunction(
            findMethod(PythonObject.class, "typeNew"),
            new PythonSignatureBuilder()
                .addParameter("cls")
                .addParameter("name")
                .addParameter("bases")
                .addParameter("attributes")
                .build()
        ));               
        type.setAttribute("__getattribute__",
            newFunction(
                findMethod(PythonObject.class, "typeGetAttribute"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .addParameter("name")
                    .build()
            )
        );
        
        method = type.callAttribute("__new__", 
            new PythonParameter()
                .addPositionalArgument(type)
                .addPositionalArgument(
                    newPythonString("method")
                )
                .addPositionalArgument(
                    newTuple(object)
                )
                .addPositionalArgument(
                    newDict(emptyMap())
                )
        );
        
        bool = type.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(type)
            .addPositionalArgument(newString("bool"))
            .addPositionalArgument(newTuple(int0))
            .addPositionalArgument(newDict(emptyMap())));
        bool.setAttribute("__new__", newFunction(findMethod(PythonBoolean.class, "newBoolean"), 
            new PythonSignatureBuilder()
                .addParameter("cls")
                .addParameter("obj")
                .build()));
        bool.setAttribute("__bool__", newFunction(
                findMethod(PythonBoolean.class, "toBoolean"),
                new PythonSignatureBuilder()
                    .addParameter("self")
                    .build()));
        
        False = new PythonBoolean();
        True = new PythonBoolean();
        
        Exception = type.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(type)
            .addPositionalArgument(newString("exception"))
            .addPositionalArgument(newTuple(object))
            .addPositionalArgument(newDict(emptyMap())));
        Exception.setAttribute("__new__", newFunction(
            findMethod(PythonBaseException.class, "newException"),
            new PythonSignatureBuilder() 
                .addParameter("cls")
                .addParameter("*args")  
                .build() 
        ));
        
        StopIteration = type.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(type)
            .addPositionalArgument(newString("StopIteration"))
            .addPositionalArgument(newTuple(Exception))
            .addPositionalArgument(newDict(emptyMap())));
       
        PythonTuple.init();
        /*
        type.addMethod(
                "__call__",
                (args, kwargs) -> {
                    PythonObject self = (PythonObject) args.get(0);

                    return self.invokeStaticMethod("__new__", args, kwargs);
                });

        /*object.addMethod(
                "__setattr__",
                (args, kwargs) -> {
                    object.setAttribute((String) args.get(1), args.get(2));

                    return null;
                });*/
    }
    
    private static PythonObject typeNew(PythonObject cls, PythonObject name, PythonObject bases, PythonObject attributes) {
        PythonObject self = new PythonObject();
        self.setAttribute("__class__", cls);
        self.setAttribute("__name__", name);
        self.setAttribute("__bases__", bases);
        
        return self;
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
        return findAttribute(self, name.toString());
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

    public PythonObject call(PythonParameter parameter) {
        throw new UnsupportedOperationException();
    }
    
    public boolean toBoolean() {
        PythonObject boolMethod = getAttribute("__bool__");
        if(boolMethod == null) {
            return true;
        }
        
        PythonObject bool = boolMethod.call(new PythonArgument());
        if(!(bool instanceof PythonBoolean)) {
            throw new IllegalArgumentException();
        }
        return bool == False ? false : true;
    }
    
    public PythonObject getItem(PythonObject key) {
        return callAttribute("__getitem__", new PythonArgument()
            .addPositionalArgument(key));
    }
    
    public void raise() {
    }
    
    private int getHashCode() {
        return super.hashCode();
    }
    
    private PythonObject callTypeAttribute(String name, PythonParameter parameter) {
        return getTypeAttribute(name).call(parameter);
    }
    
    private PythonObject getTypeAttribute(String name) {
        PythonObject type = (PythonObject)attributes.get("__class__");
        PythonObject attribute = findAttributeWithoutTypeAllowConversion(this, type, name);
        
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
        return getAttribute("__name__").toString();
    }
    
    @Override
    public Iterator<PythonObject> iterator() {
        PythonObject pythonIterator = callTypeAttribute("__iter__", new PythonArgument());
        
        return new IteratorWrapper(pythonIterator);
    }
    
    public static PythonObject newFunction(Method javaMethod, PythonSignature signature) {
        return new PythonFunction(javaMethod, signature);
    }

    public static PythonObject newString(String string) {
        PythonString object = new PythonString(string);
        object.setAttribute("__class__", str);

        return object;
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
    
    public static PythonObject newInt(int value) {
        PythonObject object = new PythonInt(value);
        object.setAttribute("__class__", int0);

        return object;
    }
    
    public static PythonObject newBoolean(boolean val) { 
        return val ? True : False;
    }
    
    public static ExceptFinally tryExcept(Runnable tryRunnable) {
        return new ExceptFinally(tryRunnable);
    }
    
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
    
    public static int unpackPythonInt(PythonObject integer) {
        if(!(integer instanceof PythonInt)) {
            throw new IllegalArgumentException("Cannot unpack non int object");
        }
        
        return ((PythonInt)integer).value;
    }
    
    private static PythonObject eq(PythonObject self, PythonObject obj) {
        return newBoolean(self == obj);
    }
    
    private static Method findMethod(Class source, String name) {
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
    
    private static class PythonDict extends PythonObject {
        
        private Map<PythonObject, PythonObject> map;
        
        private PythonDict(Map<PythonObject, PythonObject> map) {
            this.map = map;
        }
        
        private static PythonObject dictGetItem(PythonDict self, PythonObject key) {
            return self.map.get(key);
        }
    }

    private static class PythonInt extends PythonObject {

        private int value;

        public PythonInt(int value) {
            this.value = value;
        }
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

    private static class PythonString extends PythonObject {

        private String string;

        public PythonString(String string) {
            this.string = string;
        }
        
        private static PythonObject stringHash(PythonString self) {
            return newInt(self.string.hashCode());
        }
        
        private static PythonObject stringEq(PythonString self, PythonObject eq) {
            if(!(eq instanceof PythonString)) {
                return False;
            }
            
            return newBoolean(self.string.equals(((PythonString)eq).string));
        }
        
        @Override
        public String toString() {
            return string;
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
            
            this.args = args;
        }
        
        private static PythonObject newException(PythonObject cls, PythonObject args) {
            return new PythonBaseException(cls, args);
        }
        
        @Override
        public void raise() {
            throw new PythonException(this, args.toString());
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
