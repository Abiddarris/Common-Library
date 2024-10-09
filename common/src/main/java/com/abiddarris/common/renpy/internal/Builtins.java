package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.core.functions.Functions.newFunction;

import com.abiddarris.common.renpy.internal.attributes.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.core.Enumerate;
import com.abiddarris.common.renpy.internal.core.Functions;
import com.abiddarris.common.renpy.internal.core.Objects;
import com.abiddarris.common.renpy.internal.core.Property;
import com.abiddarris.common.renpy.internal.core.Slice;
import com.abiddarris.common.renpy.internal.core.Super;
import com.abiddarris.common.renpy.internal.core.classes.AttributeSetter;
import com.abiddarris.common.renpy.internal.core.classes.Classes;
import com.abiddarris.common.renpy.internal.core.classes.DelegateType;
import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;
import com.abiddarris.common.renpy.internal.mod.io.IO;
import com.abiddarris.common.renpy.internal.mod.re.Re;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Builtins {
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
    public static final PythonObject ImportError;
    public static final PythonObject slice;
    public static final PythonObject property;
    public static final PythonObject set;
    public static final PythonObject builtins;
    public static final PythonObject len;
    public static final PythonObject issubclass;
    public static final PythonObject __import__;
    public static final PythonObject isinstance;
    public static final PythonObject hasattr;
    public static final PythonObject zip;
    public static final PythonObject range;

    private static final PythonObject method;
    private static final PythonObject NoneType;

    static {
        type = newBootstrapObject();
        object = newBootstrapObject();
        tuple = newBootstrapObject();
        function = newBootstrapObject();

        PythonObject defaultBases = newTuple(object);

        object.setAttributeDirectly("__class__", type);
        object.setAttributeDirectly("__setattr__", newFunction(Objects::setAttribute, "self", "name", "value"));
        object.setAttributeDirectly("__getattribute__", newFunction(PythonObject::typeGetAttribute, "self", "name"));

        type.setAttributeDirectly("__class__", type);
        type.setAttributeDirectly("__bases__", defaultBases);
        type.setAttributeDirectly("__mro__", newTuple(type, object));

        str = newBootstrapObject();
        str.setAttributeDirectly("__class__", type);
        str.setAttribute("__bases__", defaultBases);
        str.setAttribute("__name__", PythonObject.newPythonString("str"));
        str.setAttribute("__hash__", Python.newFunction(Python.findMethod(PythonString.class, "stringHash"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()));
        str.setAttribute("__eq__", Python.newFunction(
                Python.findMethod(PythonString.class, "stringEq"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .addParameter("obj")
                        .build()
        ));
        str.setAttribute("__new__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "strNew"), "self", "obj"));
        str.setAttribute("__init__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "strInit"), "self", "obj"));
        str.setAttribute("__mro__", newTuple(str, object));
        str.setAttribute("__contains__", Python.newFunction(PythonString.class, "contains", "self", "key"));
        str.setAttribute("__mul__", Python.newFunction(PythonString.class, "multiply", "self", "value"));

        str.addNewFunction("__getitem__", PythonString.class, "getItem", "self", "key");
        str.addNewFunction("__len__", PythonString.class, "len", "self");
        str.addNewFunction("replace", PythonString.class, "replace", "self", "old", "new");
        str.addNewFunction("join", PythonString.class, "join", "self", "iterable");

        int0 = newBootstrapObject();
        int0.setAttributeDirectly("__class__", type);
        int0.setAttribute("__bases__", defaultBases);
        int0.setAttribute("__name__", PythonObject.newPythonString("int"));
        int0.setAttribute("__hash__", Python.newFunction(PythonInt.class, "hash", "self"));
        int0.setAttribute("__eq__", Python.newFunction(PythonInt.class, "eq", "self", "other"));
        int0.setAttribute("__mro__", newTuple(int0, object));
        int0.setAttribute("__gt__", Python.newFunction(PythonInt.class, "greaterThan", "self", "value"));
        int0.setAttribute("__lt__", Python.newFunction(PythonInt.class, "lessThan", "self", "value"));
        int0.setAttribute("__sub__", Python.newFunction(PythonInt.class, "subtract", "self", "value"));

        int0.addNewFunction("__add__", PythonInt.class, "add", "self", "value");
        int0.addNewFunction("__bool__", PythonInt.class, "bool", "self");
        int0.addNewFunction("__str__", PythonInt.class, "str", "self");

        function.setAttributeDirectly("__class__", type);
        function.setAttribute("__bases__", defaultBases);
        function.setAttribute("__name__", PythonObject.newPythonString("function"));
        function.setAttribute("__mro__", newTuple(function, object));

        len = Python.newFunction(Python.findMethod(BuiltinsImpl.class, "len"), "obj");

        object.setAttribute("__name__", PythonObject.newPythonString("object"));
        object.setAttribute("__new__", Python.newFunction(
                Python.findMethod(PythonObject.class, "pythonObjectNew"),
                new PythonSignatureBuilder()
                        .addParameter("cls")
                        .build()
        ));
        object.setAttribute("__hash__",
                Python.newFunction(
                        Python.findMethod(PythonObject.class, "objectHash"),
                        new PythonSignatureBuilder()
                                .addParameter("self")
                                .build()
                )
        );
        object.setAttribute("__eq__", Python.newFunction(
                Python.findMethod(PythonObject.class, "eq"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .addParameter("obj")
                        .build()
        ));
        object.setAttribute("__init__", Python.newFunction(
                Python.findMethod(PythonObject.class, "objectInit"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()
        ));
        object.setAttribute("__str__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "objectStr"), "self"));
        object.setAttribute("__bases__", newTuple());
        object.setAttribute("__ne__", Python.newFunction(BuiltinsImpl.class, "objectNe", "self", "other"));
        object.setAttribute("__mro__", newTuple(object));

        tuple.setAttributeDirectly("__class__", type);
        tuple.setAttribute("__bases__", defaultBases);
        tuple.setAttribute("__name__", PythonObject.newPythonString("tuple"));
        tuple.setAttribute("__getitem__", Python.newFunction(
                Python.findMethod(PythonTuple.class, "getitem"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .addParameter("index")
                        .build()
        ));
        tuple.setAttribute("__len__", Python.newFunction(
                Python.findMethod(PythonTuple.class, "len"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()
        ));
        tuple.setAttribute("__iter__", Python.newFunction(
                Python.findMethod(PythonTuple.class, "iter"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()
        ));
        tuple.setAttribute("__str__", Python.newFunction(PythonTuple.class, "str", "self"));
        tuple.setAttribute("__mro__", newTuple(tuple, object));

        tuple.addNewFunction("__contains__", PythonTuple.class, "contains", "self", "value");

        dict = newBootstrapObject();
        dict.setAttributeDirectly("__class__", type);
        dict.setAttribute("__bases__", defaultBases);
        dict.setAttribute("__name__", PythonObject.newPythonString("dict"));
        dict.setAttribute("__getitem__", Python.newFunction(
                Python.findMethod(PythonDict.class, "dictGetItem"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .addParameter("key")
                        .build()
        ));
        dict.setAttribute("__iter__", Python.newFunction(
                Python.findMethod(PythonDict.class, "iter"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()
        ));
        dict.setAttribute("__setitem__", Python.newFunction(Python.findMethod(PythonDict.class, "setItem"), "self", "key", "value"));
        dict.setAttribute("__contains__", Python.newFunction(PythonDict.class, "contains", "self", "value"));
        dict.setAttribute("__len__", Python.newFunction(PythonDict.class, "len", "self"));
        dict.setAttribute("__str__", Python.newFunction(PythonDict.class, "str", "self"));
        dict.setAttribute("update", Python.newFunction(PythonDict.class, "update", "self", "other"));
        dict.setAttribute("__new__", Python.newFunction(PythonDict.class, "new0", "cls"));
        dict.setAttribute("__mro__", newTuple(dict, object));
        type.setAttribute("__name__", PythonObject.newPythonString("type"));

        type.setAttribute("__new__", Python.newFunction(
                Python.findMethod(Classes.class, "typeNew"), "cls", "*args"));
        type.setAttribute("__getattribute__", newFunction(PythonObject::typeGetAttribute, "self", "name"));
        type.setAttribute("__init__", Python.newFunction(
                Python.findMethod(Classes.class, "typeInit"), "cls", "*args"
        ));
        type.setAttribute("__call__", Python.newFunction(
                Python.findMethod(PythonObject.class, "typeCall"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .addParameter("*args")
                        .addParameter("**kwargs")
                        .build()
        ));
        type.setAttribute("__subclasscheck__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "typeSubclassCheck"), "self", "other"));
        type.setAttribute("__str__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "typeStr"), "self"));
        type.setAttribute("__instancecheck__", Python.newFunction(BuiltinsImpl.class, "objectInstanceCheck", "self", "other"));

        method = Bootstrap.newClass(type, newTuple(PythonObject.newPythonString("method"), newTuple(object)));

        Exception = Bootstrap.newClass(type, newTuple(newString("exception"), newTuple()), new BootstrapAttributeHolder());
        Exception.setAttribute("__new__", Python.newFunction(
                Python.findMethod(PythonObject.PythonBaseException.class, "newException"),
                new PythonSignatureBuilder()
                        .addParameter("cls")
                        .addParameter("*args")
                        .build()
        ));
        Exception.setAttribute("__init__", Python.newFunction(Python.findMethod(PythonObject.PythonBaseException.class, "init"), "self", "*args"));

        StopIteration = Bootstrap.newClass(type, newTuple(newString("StopIteration"), newTuple(Exception)), new BootstrapAttributeHolder());
        KeyError = Bootstrap.newClass(type, newTuple(newString("KeyError"), newTuple(Exception)), new BootstrapAttributeHolder());

        PythonDict.init();

        bool = Bootstrap.newClass(type, newTuple(newString("bool"), newTuple(int0)), new BootstrapAttributeHolder());
        bool.setAttribute("__new__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "boolNew"),
                "cls", "obj"));
        bool.setAttribute("__init__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "boolInit"),
                "cls", "obj"));
        bool.setAttribute("__bool__", Python.newFunction(
                Python.findMethod(PythonObject.PythonBoolean.class, "toBoolean"),
                new PythonSignatureBuilder()
                        .addParameter("self")
                        .build()));

        False = new PythonObject.PythonBoolean(0);
        True = new PythonObject.PythonBoolean(1);

        PythonTuple.init();

        AttributeSetter.activate();

        TypeError = type.call(newString("TypeError"), newTuple(Exception), Python.newDict());
        AttributeError = type.call(newString("AttributeError"), newTuple(Exception), Python.newDict());

        DelegateType.activate();

        issubclass = Python.newFunction(Python.findMethod(Functions.class, "issubclass"), "cls", "base");

        NoneType = type.call(newString("NoneType"), newTuple(type), Python.newDict());
        None = NoneType.call(newString("None"), newTuple(), Python.newDict());

        NoneType.setAttribute("__new__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "noneTypeNew"), "cls"));
        NoneType.setAttribute("__init__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "noneTypeInit"), "cls"));
        NoneType.setAttribute("__call__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "noneTypeCall"), "self"));
        NoneType.setAttribute("__bool__", Python.newFunction(Python.findMethod(BuiltinsImpl.class, "noneTypeBool"), "self"));

        str.setAttribute("rsplit", Python.newFunction(PythonString.class, "rsplit",
                new PythonSignatureBuilder("self")
                        .addParameter("sep", None)
                        .addParameter("maxsplit", Python.newInt(-1))
                        .build()));
        str.setAttribute("__add__", Python.newFunction(PythonString.class, "add", "self", "value"));
        str.setAttribute("startswith", Python.newFunction(PythonString.class, "startsWith", "self", "prefix"));
        str.setAttribute("count", Python.newFunction(PythonString.class, "count", "self", "sub"));
        str.setAttribute("format", Python.newFunction(PythonString.class, "format", "self", "*args"));

        dict.addNewFunction("get", PythonDict.class, "get", new PythonSignatureBuilder("self", "key")
                .addParameter("default", None)
                .build());

        Types.init();

        builtins = Python.createModule("builtins");

        PythonList.init();

        __import__ = Python.newFunction(Python.findMethod(BuiltinsImpl.class, "importImpl"), "name");
        list = Python.newClass("list", newTuple(), Python.newDict(
                newString("__getitem__"), Python.newFunction(Python.findMethod(PythonList.class, "getItem"), "self", "index"),
                newString("insert"), Python.newFunction(Python.findMethod(PythonList.class, "insert"), "self", "index", "element"),
                newString("__iter__"), Python.newFunction(Python.findMethod(PythonList.class, "iter"), "self"),
                newString("append"), Python.newFunction(PythonList.class, "append", "self", "append"),
                newString("pop"), Python.newFunction(PythonList.class, "pop", "self"),
                newString("__len__"), Python.newFunction(PythonList.class, "len", "self"),
                newString("__setitem__"), Python.newFunction(Python.findMethod(PythonList.class, "setItem"), "self", "key", "value"),
                newString("extend"), newFunction(PythonList::extend, "self", "*iterable"),
                newString("__new__"), newFunction(PythonList::new0, "self", "iterable"),
                newString("__init__"), newFunction(PythonList::init0, "self", "iterable")
        ));

        Sys.init();

        IndexError = Python.newClass("IndexError", newTuple(Exception), Python.newDict());
        ModuleNotFoundError = Python.newClass("ModuleNotFoundError", newTuple(Exception), Python.newDict());

        JavaModuleLoader.init();

        isinstance = Python.newFunction(Functions.class, "isInstance", "instance", "class");
        hasattr = Python.newFunction(BuiltinsImpl.class, "hasAttr", "obj", "name");

        int0.defineAttribute("__module__", newString("builtins"));
        int0.defineFunction("__ge__", PythonInt::ge, "self", "value");

        ValueError = builtins.defineClass("ValueError", Exception).define();

        super0 = Super.define(builtins);

        RuntimeError = builtins.defineClass("RuntimeError", Exception).define();
        ImportError = builtins.defineClass("ImportError", Exception).define();
        NotImplementedError = builtins.defineClass("NotImplementedError", RuntimeError).define();

        enumerate = Enumerate.define(builtins);
        slice = Slice.define(builtins);
        property = Property.define(builtins);

        IO.initLoader();
        com.abiddarris.common.renpy.internal.mod.builtins.BuiltinsImpl.initRest();

        set = builtins.getAttribute("set");
        zip = builtins.getAttribute("zip");
        range = builtins.getAttribute("range");

        Re.initLoader();
    }

    private static PythonObject newBootstrapObject() {
        return new PythonObject(new BootstrapAttributeHolder());
    }
}
