package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonSyntax.getAttr;

import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignature;
import java.lang.reflect.Method;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PythonObject {

    public static final PythonObject object;
    public static final PythonObject type;
    public static final PythonObject function;
    public static final PythonObject str;
    public static final PythonObject tuple;

    static {
        str = new PythonObject();

        function = new PythonObject();
        function.addField("__name__", newPythonString("function"));

        tuple = new PythonObject();
        tuple.addField("__name__", newPythonString("tuple"));

        type = new PythonObject();
        type.addField("__name__", "type");
        type.addMethod(
                "__new__",
                (args, kwargs) -> {
                    PythonObject object = new PythonObject();
                    object.addField("__name__", args.get(1));
                    object.addField("__bases__", args.get(2));

                    return object;
                });
        type.addMethod(
                "__call__",
                (args, kwargs) -> {
                    PythonObject self = (PythonObject) args.get(0);

                    return self.invokeStaticMethod("__new__", args, kwargs);
                });

        object =
                type.invokeStaticMethod(
                        "__new__", List.of(type, emptyList(), emptyList(), emptyMap()), emptyMap());
        object.addMethod(
                "__setattr__",
                (args, kwargs) -> {
                    object.setAttribute((String) args.get(1), args.get(2));

                    return null;
                });
        object.addMethod(
                "__new__",
                (args, kwargs) -> {
                    return new PythonObject();
                });
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

        List<PythonObject> classes = (List<PythonObject>) attributes.get("__bases__");
        if (classes.isEmpty()) {
            classes = List.of(object);
        }
        for (var class0 : classes) {
            if (class0.hasAttribute(name)) {
                return class0.getAttribute(name);
            }
        }

        throw new NoSuchElementException();
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
        return (String) getAttribute("__name__");
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

    private static class PythonTuple extends PythonObject {

        private PythonObject[] elements;

        public PythonTuple(PythonObject[] elements) {
            this.elements = elements;
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
