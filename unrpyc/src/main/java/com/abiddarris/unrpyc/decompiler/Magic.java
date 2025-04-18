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
 *
 * Original MIT License :
 *
 * Copyright (c) 2015-2024 CensoredUsername
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *************************************************************************************/
package com.abiddarris.unrpyc.decompiler;

import static com.abiddarris.python3.Python.createModule;
import static com.abiddarris.python3.Python.newDict;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.Builtins.False;
import static com.abiddarris.python3.Builtins.KeyError;
import static com.abiddarris.python3.Builtins.None;
import static com.abiddarris.python3.Builtins.TypeError;
import static com.abiddarris.python3.Builtins.__import__;
import static com.abiddarris.python3.Builtins.dict;
import static com.abiddarris.python3.Builtins.isinstance;
import static com.abiddarris.python3.Builtins.len;
import static com.abiddarris.python3.PythonObject.newBoolean;
import static com.abiddarris.python3.PythonObject.newInt;
import static com.abiddarris.python3.PythonObject.newList;
import static com.abiddarris.python3.Builtins.super0;
import static com.abiddarris.python3.PythonObject.tryExcept;
import static com.abiddarris.python3.Builtins.tuple;
import static com.abiddarris.python3.Builtins.type;
import static com.abiddarris.python3.core.Attributes.callNestedAttribute;
import static com.abiddarris.python3.core.Attributes.getNestedAttribute;
import static com.abiddarris.python3.core.Functions.any;
import static com.abiddarris.python3.core.Functions.bool;
import static com.abiddarris.python3.core.Functions.hasattr;
import static com.abiddarris.python3.core.Functions.hash;
import static com.abiddarris.python3.core.Functions.isInstance;
import static com.abiddarris.python3.core.Functions.isinstance;
import static com.abiddarris.python3.core.JFunctions.getattr;
import static com.abiddarris.python3.core.JFunctions.hasattr;
import static com.abiddarris.common.stream.Signs.sign;

import com.abiddarris.python3.Pickle;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.core.Functions;
import com.abiddarris.python3.loader.JavaModuleLoader;
import com.abiddarris.python3.signature.PythonArgument;
import com.abiddarris.python3.signature.PythonSignatureBuilder;
import com.abiddarris.common.utils.ObjectWrapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This module provides tools for safely analyizing pickle files programmatically */
public class Magic {

    private static PythonObject magic;

    static void initLoader() {
        JavaModuleLoader.registerLoader("decompiler.magic", (name) -> {
            magic = createModule("decompiler.magic");
            magic.importModule("sys");
            magic.importModule("types");

            magic.fromImport("importlib.machinery", "ModuleSpec");
            magic.fromImport("decompiler.unrpyccompat", "FakeModuleSubclassCheckGenerator");

            PythonObject FakeClassType = magic.addNewClass("FakeClassType", type);
                
            FakeClassTypeImpl.initObject(FakeClassType);
            
            PythonObject FakeClass = FakeClassType.call(
                new PythonArgument(
                    newString("FakeClass"), newTuple(), newDict(
                        newString("__doc__"), newString("A barebones instance of :class:`FakeClassType`. Inherit from this to create fake classes.")
                    )
                ).addKeywordArgument("module", magic.getAttribute("__name__"))
            );
          
            magic.setAttribute("FakeClass", FakeClass);
                
            PythonObject FakeStrict = FakeStrictImpl.define(magic, FakeClass);

            FakeModuleImpl.define(magic);
            FakePackageImpl.define(magic);
            FakePackageLoaderImpl.define(magic);

            magic.addNewFunction("fake_package", Magic.class, "fakePackage", "name");

            return magic;    
        });
    }
    
    /**
     * The metaclass used to create fake classes. To support comparisons between fake classes and
     * :class:`FakeModule` instances custom behaviour is defined here which follows this logic:
     *
     * <p>If the other object does not have ``other.__name__`` set, they are not equal.
     *
     * <p>Else if it does not have ``other.__module__`` set, they are equal if ``self.__module__ +
     * "." + self.__name__ == other.__name__``.
     *
     * <p>Else, they are equal if ``self.__module__ == other.__module__ and self.__name__ ==
     * other.__name__``
     *
     * <p>Using this behaviour, ``==``, ``!=``, ``hash()``, ``isinstance()`` and ``issubclass()``
     * are implemented allowing comparison between :class:`FakeClassType` instances and
     * :class:`FakeModule` instances to succeed if they are pretending to be in the same place in
     * the python module hierarchy.
     *
     * <p>To create a fake class using this metaclass, you can either use this metaclass directly or
     * inherit from the fake class base instances given below. When doing this, the module that this
     * fake class is pretending to be in should be specified using the *module* argument when the
     * metaclass is called directly or a :attr:``__module__`` class attribute in a class statement.
     *
     * <p>This is a subclass of :class:`type`.
     */
    private static class FakeClassTypeImpl {
        
        private static void initObject(PythonObject FakeClassType) {
            FakeClassType.addNewFunction("__new__", FakeClassTypeImpl.class, "new0",
                 new PythonSignatureBuilder("cls", "name", "bases", "attributes")
                    .addParameter("module", None)
                    .build());
            FakeClassType.addNewFunction("__init__", FakeClassTypeImpl.class, "init",
                 new PythonSignatureBuilder("self", "name", "bases", "attributes")
                    .addParameter("module", None)
                    .build());
            FakeClassType.addNewFunction("__eq__", FakeClassTypeImpl.class, "eq", "self", "other");
            FakeClassType.addNewFunction("__hash__", FakeClassTypeImpl.class, "hash0", "self");
        }
        
        private static PythonObject new0(PythonObject cls, PythonObject name, PythonObject bases, PythonObject attributes, PythonObject module) {
            // This would be a lie
            //attributes.pop("__qualname__", None)

            // figure out what module we should say we're in
            // note that if no module is explicitly passed, the current module will be chosen
            // due to the class statement implicitly specifying __module__ as __name__
            if (module != None) 
                attributes.setItem(newString("__module__"), module);

            if (!attributes.jin(newString("__module__"))) {
                TypeError.call(newString(String.format(
                    "No module has been specified for FakeClassType %s", name)))
                    .raise();
            }

            // assemble instance
            return type.callAttribute("__new__", cls, name, bases, attributes);
        }
        
        private static PythonObject init(PythonObject self, PythonObject name, PythonObject bases, PythonObject attributes, PythonObject module) {
            return type.callAttribute("__init__", self, name, bases, attributes);
        }

        private static PythonObject eq(PythonObject self, PythonObject other) {
             if (!hasattr(other, newString("__name__")).toBoolean()) {
                 return False;
             }

             if (!isinstance(other, magic.getAttribute("FakeModule")).toBoolean()
                     && hasattr(other, newString("__module__")).toBoolean()) {
                 return newBoolean(self.getAttribute("__module__").equals(other.getAttribute("__module__"))
                        && self.getAttribute("__name__").equals(other.getAttribute("__name__")));
             } else {
                 return self.getAttribute("__module__")
                         .add(newString("."))
                         .add(self.getAttribute("__name__"))
                         .pEquals(other.getAttribute("__name__"));
             }
        }

        private static PythonObject hash0(PythonObject self) {
            return hash(self.getAttribute("__module__")
                    .add(newString("."))
                    .add(self.getAttribute("__name__")));
        }
    }
    
    private static class FakeStrictImpl {
        
        private static PythonObject magic;
        
        private static PythonObject define(PythonObject magic, PythonObject FakeClass) {
            FakeStrictImpl.magic = magic;
            
            ClassDefiner definer = magic.defineClass("FakeStrict", FakeClass);
            definer.defineFunction("__new__", FakeStrictImpl.class, "new0", "cls", "*args", "**kwargs");
            definer.defineFunction("__setstate__", FakeStrictImpl.class, "setState", "self", "state");
            
            return definer.define();
        }
        
        private static PythonObject new0(PythonObject cls, PythonObject args, PythonObject kwargs) {
            PythonObject self = magic.getAttribute("FakeClass")
                .callAttribute("__new__", cls);
            
            if (args.toBoolean() || kwargs.toBoolean()) {
                throw new FakeUnpicklingError(
                    String.format(
                        "%s was instantiated with unexpected arguments %s, %s",
                        cls, args, kwargs));
            }
                
            return self;
        }
        
        private static void setState(PythonObject self, PythonObject state) {
            PythonObject slotstate = None;

            if (isinstance.call(state, tuple).toBoolean() && len.call(state).toInt() == 2 &&
                (state.getItem(newInt(0)) == None || isinstance.call(state.getItem(newInt(0)), dict).toBoolean()) &&
                (state.getItem(newInt(1)) == None || isinstance.call(state.getItem(newInt(1)), dict).toBoolean())) {
                slotstate = state.getItem(newInt(1));
                state = state.getItem(newInt(0));
            }
                
            if (state.toBoolean()) {
                // Don't have to check for slotstate here since it's either None or a dict
                if (!isinstance.call(state, dict).toBoolean()) {
                    throw new FakeUnpicklingError(String.format("%s.__setstate__() got unexpected arguments %s", self.getAttribute("__class__"), state));
                } else {
                    self.getAttribute("__dict__")
                        .callAttribute("update", state);
                }
            }
            
            if (slotstate.toBoolean()) {
                self.getAttribute("__dict__")
                    .callAttribute("update", slotstate);
            }
        }
    }
    
    /*
         # comparison logic


         def __ne__(self, other):
             return not self == other



         def __instancecheck__(self, instance):
             return self.__subclasscheck__(instance.__class__)

         def __subclasscheck__(self, subclass):
             return (self == subclass or
                     (bool(subclass.__bases__) and
                      any(self.__subclasscheck__(base) for base in subclass.__bases__)))
         
    }*/

    /**
     * An object which pretends to be a module.
     *
     * *name* is the name of the module and should be a ``"."`` separated
     * alphanumeric string.
     *
     * On initialization the module is added to sys.modules so it can be
     * imported properly. Further if *name* is a submodule and if its parent
     * does not exist, it will automatically create a parent :class:`FakeModule`.
     * This operates recursively until the parent is a top-level module or
     * when the parent is an existing module.
     *
     * If any fake submodules are removed from this module they will
     * automatically be removed from :data:`sys.modules`.
     *
     * Just as :class:`FakeClassType`, it supports comparison with
     * :class:`FakeClassType` instances, using the following logic:
     *
     * If the object does not have ``other.__name__`` set, they are not equal.
     *
     * Else if the other object does not have ``other.__module__`` set, they are equal if:
     * ``self.__name__ == other.__name__``
     *
     * Else, they are equal if:
     * ``self.__name__ == other.__module__ + "." + other.__name__``
     *
     * Using this behaviour, ``==``, ``!=``, ``hash()``, ``isinstance()`` and ``issubclass()``
     * are implemented allowing comparison between :class:`FakeClassType` instances
     * and :class:`FakeModule` instances to succeed if they are pretending to bein the same
     * place in the python module hierarchy.
     *
     * It inherits from :class:`types.ModuleType`.
     */
    private static class FakeModuleImpl {

        private static PythonObject magic;

        private static PythonObject define(PythonObject magic) {
            FakeModuleImpl.magic = magic;

            ClassDefiner define = magic.defineClass("FakeModule", getNestedAttribute(magic, "types.ModuleType"));
            define.defineFunction("__init__", FakeModuleImpl.class, "init", "self", "name");
            define.defineFunction("__instancecheck__", FakeModuleImpl.class, "instanceCheck", "self", "instance");
            define.defineFunction("__subclasscheck__", FakeModuleImpl.class, "subclassCheck", "self", "subclass");
            define.defineFunction("__eq__", FakeModuleImpl.class, "eq", "self", "other");
            define.defineFunction("__ne__", FakeModuleImpl.class, "ne", "self", "other");
            define.defineFunction("__hash__", FakeModuleImpl.class, "hash", "self");

            return define.define();
        }

        private static void init(PythonObject self, PythonObject name) {
            super0.call(magic.getAttribute("FakeModule"), self).callAttribute("__init__", name);

            getNestedAttribute(magic, "sys.modules").setItem(name, self);

            if (name.jin(newString("."))) {
                PythonObject names = name.callAttribute("rsplit", newString("."), newInt(1));
                PythonObject parent_name = names.getItem(newInt(0));
                PythonObject child_name = names.getItem(newInt(1));

                ObjectWrapper<PythonObject> parent = new ObjectWrapper<>();
                tryExcept(() -> {
                    __import__.call(parent_name);
                    parent.setObject(getNestedAttribute(magic, "sys.modules").getItem(parent_name));
                }).onExcept((e) -> parent.setObject(magic.getAttribute("FakeModule").call(parent_name)), KeyError).execute();

                parent.getObject().setAttribute(child_name, self);
            }
        }

        private static PythonObject eq(PythonObject self, PythonObject other) {
            if (!hasattr(other, newString("__name__")).toBoolean()) {
                return False;
            }

            PythonObject othername = other.getAttribute("__name__");
            if (hasattr(other, newString("__module__")).toBoolean()) {
                othername = other.getAttribute("__module__")
                        .add(newString("."))
                        .add(other.getAttribute("__name__"));
            }

            return self.getAttribute("__name__").pEquals(othername);
        }

        private static PythonObject ne(PythonObject self, PythonObject other) {
            return newBoolean(!self.equals(other));
        }

        private static PythonObject hash(PythonObject self) {
            return Functions.hash(self.getAttribute("__name__"));
        }

        private static PythonObject instanceCheck(PythonObject self, PythonObject instance) {
            return self.callAttribute("__subclasscheck__", instance.getAttribute("__class__"));
        }

        private static PythonObject subclassCheck(PythonObject self, PythonObject subclass) {
            return newBoolean(
                    self.equals(subclass) ||
                            (bool(subclass.getAttribute("__bases__")).toBoolean() &&
                                    any(magic.callAttribute("FakeModuleSubclassCheckGenerator", subclass.getAttribute("__bases__"), self)).toBoolean())
            );
        }

    }

    /**
     * <p>A :class:`FakeModule` subclass which lazily creates :class:`FakePackage`
     * instances on its attributes when they're requested.
     *
     * <p>This ensures that any attribute of this module is a valid FakeModule
     * which can be used to compare against fake classes.
     */
    private static class FakePackageImpl {

        private static PythonObject define(PythonObject magic) {
            ClassDefiner definer = magic.defineClass("FakePackage", magic.getAttribute("FakeModule"));
            definer.defineAttribute("__path__", newList());
            definer.defineFunction("__getattr__", FakePackageImpl.class, "getAttr", "self", "name");

            return definer.define();
        }

        private static PythonObject getAttr(PythonObject self, PythonObject name) {
            PythonObject modname = self.getAttribute("__name__")
                        .add(newString(".")).add(name);
            PythonObject mod = callNestedAttribute(magic, "sys.modules.get", modname, None);

            ObjectWrapper<PythonObject> wMod = new ObjectWrapper<>();
            if (mod == None) {
                tryExcept(() -> __import__.call(modname))
                        .onExcept(() -> wMod.setObject(magic.getAttribute("FakePackage")))
                        .onElse(() -> wMod.setObject(getNestedAttribute(magic, "sys.modules")
                                .getItem(modname)))
                        .execute();

                mod = wMod.getObject();
            }
            return mod;
        }

    }

    /**
     * <p>A :term:`loader` of :class:`FakePackage` modules. When added to
     * :data:`sys.meta_path` it will ensure that any attempt to import
     * module *root* or its submodules results in a FakePackage.
     *
     * <p>Together with the attribute creation from :class:`FakePackage`
     * this ensures that any attempt to get a submodule from module *root*
     * results in a FakePackage, creating the illusion that *root* is an
     * actual package tree.
     *
     * <p>This class is both a `finder` and a `loader`
     */
    private static class FakePackageLoaderImpl {

        private static PythonObject define(PythonObject magic) {
            ClassDefiner definer = magic.defineClass("FakePackageLoader");
            definer.defineFunction("__init__", FakePackageLoaderImpl.class, "init", "self", "root");
            definer.defineFunction("find_spec", FakePackageLoaderImpl.class, "findSpec", new PythonSignatureBuilder("self", "fullname", "path")
                    .addParameter("target", None)
                    .build());
            definer.defineFunction("load_module", FakePackageLoaderImpl.class, "loadModule", "self", "fullname");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject root) {
            self.setAttribute("root", root);
        }

        /**
         * the new way of loading modules. It returns a ModuleSpec, that has
         * the loader attribute set to this class.
         */
        private static PythonObject findSpec(PythonObject self, PythonObject fullname, PythonObject path, PythonObject target) {
            if (fullname.equals(self.getAttribute("root")) || fullname.callAttribute("startswith", self.getAttribute("root").add(newString("."))).toBoolean()) {
                return magic.getAttribute("ModuleSpec").call(fullname, self);
            } else {
                return None;
            }
        }

        /**
         * loader methods. This loads the module.
         */
        private static PythonObject loadModule(PythonObject self, PythonObject fullname) {
            return magic.getAttribute("FakePackage").call(fullname);
        }
    }

    /**
     * <p>Mounts a fake package tree with the name *name*. This causes any attempt to import
     * module *name*, attributes of the module or submodules will return a :class:`FakePackage`
     * instance which implements the same behaviour. These :class:`FakePackage` instances compare
     * properly with :class:`FakeClassType` instances allowing you to code using FakePackages as
     * if the modules and their attributes actually existed.
     *
     * <p>This is implemented by creating a :class:`FakePackageLoader` instance with root *name*
     * and inserting it in the first spot in :data:`sys.meta_path`. This ensures that importing the
     * module and submodules will work properly. Further the :class:`FakePackage` instances take
     * care of generating submodules as attributes on request.
     *
     * <p>If a fake package tree with the same *name* is already registered, no new fake package
     * tree will be mounted.
     *
     * <p>This returns the :class:`FakePackage` instance *name*.
     */
    private static PythonObject fakePackage(PythonObject name) {
        if (getNestedAttribute(magic, "sys.modules").jin(name) &&
                isInstance(getNestedAttribute(magic, "sys.modules").getItem(name), magic.getAttribute("FakePackage")).toBoolean()) {
            return getNestedAttribute(magic, "sys.modules").getItem(name);
        } else {
            PythonObject loader = magic.getAttribute("FakePackageLoader").call(name);

            getNestedAttribute(magic, "sys.meta_path").callAttribute("insert", newInt(0), loader);
            return __import__.call(name);
        }
    }

    /**
     * Factory of fake classses. It will create fake class definitions on demand based on the passed
     * arguments
     */
    public static class FakeClassFactory {

        private PythonObject default0;
        private Map<List<String>, PythonObject> special_cases = new LinkedHashMap<>();
        private Map<List<String>, PythonObject> class_cache = new HashMap<>();
        
        /**
         * special_cases* should be an iterable containing fake classes which should be treated as
         * special cases during the fake unpickling process. This way you can specify custom methods
         * and attributes on these classes as they're used during unpickling.
         *
         * <p>default_class* should be a FakeClassType instance which will be subclassed to create
         * the necessary non-special case fake classes during unpickling. This should usually be set
         * to :class:`FakeStrict`, :class:`FakeWarning` or :class:`FakeIgnore`. These classes have
         * :meth:`__new__` and :meth:`__setstate__` methods which extract data from the pickle
         * stream and provide means of inspecting the stream when it is not clear how the data
         * should be interpreted.
         *
         * <p>As an example, we can define the fake class generated for definition bar in module
         * foo, which has a :meth:`__str__` method which returns ``"baz"``::
         *
         * <p>class bar(FakeStrict, object): def __str__(self): return "baz"
         *
         * <p>special_cases = [bar]
         *
         * <p>Alternatively they can also be instantiated using :class:`FakeClassType` directly::
         * special_cases = [FakeClassType(c.__name__, c.__bases__, c.__dict__, c.__module__)]
         */
        public FakeClassFactory() {
            this(Collections.EMPTY_LIST, null);
        }
        
        public FakeClassFactory(
                List<PythonObject> /*ImmutableList*/ special_cases /*=()*/, PythonObject default_class /*=FakeStrict*/) {
            for(PythonObject i : special_cases) {
            	this.special_cases.put(List.of(
                    i.getAttribute("__module__").toString(),
                    i.getAttribute("__name__").toString()
                ), i);
            }
            this.default0 = default_class;
        }

        /**
         * Return the right class for the specified *module* and *name*.
         *
         * <p>This class will either be one of the special cases in case the name and module match,
         * or a subclass of *default_class* will be created with the correct name and module.
         *
         * <p>Created class definitions are cached per factory instance.
         */
        public PythonObject __call__(String name, String module) {
            // Check if we've got this class cached
            PythonObject klass = this.class_cache.get(List.of(module, name));
            if(klass != null)
                return klass;

            klass = this.special_cases.getOrDefault(List.of(module, name), None);

            if (!klass.toBoolean()) {
                // generate a new class def which inherits from the default fake class
                klass = type.call(newString(name), newTuple(this.default0), newDict(newString("__module__"), newString(module)));
            }
            
            this.class_cache.put(List.of(module, name), klass);
            return klass;
        }
    }

    /**
     * A forgiving unpickler. On uncountering references to class definitions in the pickle stream
     * which it cannot locate, it will create fake classes and if necessary fake modules to house
     * them in. Since it still allows access to all modules and builtins, it should only be used to
     * unpickle trusted data.
     *
     * <p>file* is the :term:`binary file` to unserialize.
     *
     * <p>The optional keyword arguments are *class_factory*, *encoding and *errors*. class_factory*
     * can be used to control how the missing class definitions are created. If set to ``None``,
     * ``FakeClassFactory((), FakeStrict)`` will be used.
     *
     * <p>In Python 3, the optional keyword arguments *encoding* and *errors* can be used to
     * indicate how the unpickler should deal with pickle streams generated in python 2,
     * specifically how to deal with 8-bit string instances. If set to "bytes" it will load them as
     * bytes objects, otherwise it will attempt to decode them into unicode using the given
     * *encoding* and *errors* arguments.
     *
     * <p>It inherits from :class:`pickle.Unpickler`. (In Python 3 this is actually
     * ``pickle._Unpickler``)
     */
    public static class FakeUnpickler extends Pickle.Unpickler {

        protected FakeClassFactory class_factory;
        
        public FakeUnpickler(InputStream file, FakeClassFactory class_factory/*=None*/, String encoding/*="bytes"*/, String errors/*="strict"*/) {
            super(file, false, encoding, errors);
           
            this.class_factory = class_factory == null ? new FakeClassFactory() : class_factory;
        }
        
        @Override
        protected PythonObject find_class(String module, String name) {
            return null; //this.class_factory.__call__();// new PythonObject(name);
        }
        
            /*
        def find_class(self, module, name):
            mod = sys.modules.get(module, None)
            if mod is None:
                try:
                    __import__(module)
                except:
                    mod = FakeModule(module)
                else:
                    mod = sys.modules[module]

            klass = getattr(mod, name, None)
            if klass is None or isinstance(klass, FakeModule):
                klass = self.class_factory(name, module)
                setattr(mod, name, klass)

            return klass*/
    }
    
    /**
     * Error raised when there is not enough information to perform the fake
     * unpickling process completely. It inherits from :exc:`pickle.UnpicklingError`.
     */
    public static class FakeUnpicklingError extends Pickle.UnpicklingError {
        
        public FakeUnpicklingError(String message) {
            super(message);
        }
        
    }

    /**
     * A safe unpickler. It will create fake classes for any references to class definitions in the
     * pickle stream. Further it can block access to the extension registry making this unpickler
     * safe to use on untrusted data.
     *
     * <p>file* is the :term:`binary file` to unserialize.
     *
     * <p>The optional keyword arguments are *class_factory*, *safe_modules*, *use_copyreg*,
     * encoding* and *errors*. *class_factory* can be used to control how the missing class
     * definitions are created. If set to ``None``, ``FakeClassFactory((), FakeStrict)`` will be
     * used. *safe_modules* can be set to a set of strings of module names, which will be regarded
     * as safe by the unpickling process, meaning that it will import objects from that module
     * instead of generating fake classes (this does not apply to objects in submodules).
     * *use_copyreg* is a boolean value indicating if it's allowed to use extensions from the pickle
     * extension registry (documented in the :mod:`copyreg` module).
     *
     * <p>In Python 3, the optional keyword arguments *encoding* and *errors* can be used to
     * indicate how the unpickler should deal with pickle streams generated in python 2,
     * specifically how to deal with 8-bit string instances. If set to "bytes" it will load them as
     * bytes objects, otherwise it will attempt to decode them into unicode using the given
     * *encoding* and *errors* arguments.
     *
     * <p>This function can be used to unpickle untrusted data safely with the default class_factory
     * when *safe_modules* is empty and *use_copyreg* is False. It inherits from
     * :class:`pickle.Unpickler`. (In Python 3 this is actually ``pickle._Unpickler``)
     *
     * <p>It should be noted though that when the unpickler tries to get a nonexistent attribute of
     * a safe module, an :exc:`AttributeError` will be raised.
     *
     * <p>This inherits from :class:`FakeUnpickler`
     */
    public static class SafeUnpickler extends FakeUnpickler {

        private final Set<String> safe_modules;
        private static PythonObject sys = __import__.call(newString("sys"));

        public SafeUnpickler(InputStream file, FakeClassFactory class_factory/*=None*/, Set<String> safe_modules/*=()*/,
                             boolean use_copyreg/*=False*/, String encoding/*="bytes"*/, String errors/*="strict"*/) {
            super(file, class_factory, encoding=encoding, errors=errors);

            this.safe_modules = safe_modules;
            // A set of modules which are safe to load
            /*self.use_copyreg = use_copyreg*/
        }
    
        @Override
        public PythonObject find_class(String module, String name) {
            if (this.safe_modules.contains(module)) {
                __import__.call(newString(module));
                PythonObject mod = sys.getAttributeItem("modules", module);
                if (!hasattr(mod, "__all__") || mod.getAttribute("__all__").jin(name)) {
                    PythonObject klass = getattr(mod, name);
                    return klass;
                }
            }

            return this.class_factory.__call__(name, module);
        }
       /* def get_extension(self, code):
            if self.use_copyreg:
                return FakeUnpickler.get_extension(self, code)
            else:
                return self.class_factory("extension_code_{0}".format(code), "copyreg")*/
    }

    /**
     * Similar to :func:`safe_load`, but takes an 8-bit string (bytes in Python 3, str in Python 2)
     * as its first argument instead of a binary :term:`file object`.
     */
    public static Object safe_loads(
            int[] string,
            FakeClassFactory class_factory /*=None*/,
            Set<String> safe_modules /*=()*/,
            boolean use_copyreg /*=False*/,
            String encoding /*="bytes"*/,
            String errors /*="errors"*/) {
        return new SafeUnpickler(new ByteArrayInputStream(sign(string)), class_factory, safe_modules, use_copyreg,
                     encoding, errors).load();
    }
}
