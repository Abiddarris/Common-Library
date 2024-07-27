package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonSyntax.getAttr;

import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class PythonObject {

    public static final PythonObject object;
    public static final PythonObject type;
    public static final PythonObject function;
    public static final PythonObject str;
    public static final PythonObject tuple;
    public static final PythonObject int0;
    public static final PythonObject dict;
    
    static {
        str = new PythonObject();

        int0 = new PythonObject();
        int0.addField("__name__", newPythonString("int"));

        function = new PythonObject();
        function.addField("__name__", newPythonString("function"));

        object = new PythonObject();
        object.setAttribute("__name__", newPythonString("object"));
        object.setAttribute("__new__", newFunction(
            findMethod(PythonObject.class, "pythonObjectNew"),
            new PythonSignatureBuilder()
                .addParameter("cls")
                .build()
        ));
        
        tuple = new PythonObject();
        tuple.addField("__name__", newPythonString("tuple"));
        tuple.addField("__getitem__", newFunction(
            findMethod(PythonTuple.class, "getItem"),
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
        
        dict = new PythonObject();
        dict.setAttribute("__name__", newPythonString("dict"));
        dict.setAttribute("__getitem__", newFunction(
            findMethod(PythonDict.class, "dictGetItem"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .addParameter("key")
                .build()
        ));
        
        type = new PythonObject();
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

    public Object getAttribute(String name) {
        if (attributes.containsKey(name)) return attributes.get(name);

        PythonObject bases = (PythonObject)attributes.get("__bases__");
        if(bases != null) {
            
        }
        
        /*if (classes.isEmpty()) {
            classes = List.of(object);
        }
        for (var class0 : classes) {
            if (class0.hasAttribute(name)) {
                return class0.getAttribute(name);
            }
        }*/
        
        PythonObject type = (PythonObject) attributes.get("__class__");
        return type.getAttribute(name);
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

    public PythonObject call(PythonParameter parameter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return getAttribute("__name__").toString();
    }
    
    public static PythonObject newFunction(Method javaMethod, PythonSignature signature) {
        return new PythonFunction(javaMethod, signature);
    }

    public static PythonObject newPythonString(String string) {
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
    
    public static PythonObject newPythonInt(int value) {
        PythonObject object = new PythonInt(value);
        object.setAttribute("__class__", int0);

        return object;
    }
    
    public static int unpackPythonInt(PythonObject integer) {
        if(!(integer instanceof PythonInt)) {
            throw new IllegalArgumentException("Cannot unpack non int object");
        }
        
        return ((PythonInt)integer).value;
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

        private PythonObject[] elements;

        public PythonTuple(PythonObject[] elements) {
            this.elements = elements;
        }
        
        private static PythonObject getItem(PythonObject self, PythonObject pos) {
            if(!(self instanceof PythonTuple)) {
                throw new IllegalArgumentException("Error");
            }
            
            return ((PythonTuple)self).elements[unpackPythonInt(pos)];
        }
        
        private static PythonObject len(PythonTuple self) {
            return newPythonInt(self.elements.length);
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

        @Override
        public String toString() {
            return string;
        }
    }
}
