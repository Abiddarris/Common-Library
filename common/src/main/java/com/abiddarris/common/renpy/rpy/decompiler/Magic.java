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
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.Python.createModule;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.TypeError;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.PythonObject.type;
import static com.abiddarris.common.stream.Signs.sign;

import com.abiddarris.common.renpy.internal.Pickle;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This module provides tools for safely analyizing pickle files programmatically */
public class Magic {
    
    static void initLoader() {
        JavaModuleLoader.registerLoader("decompiler.magic", (name) -> {
            PythonObject magic = createModule("decompiler.magic");
            PythonObject FakeClassType = magic.addNewClass("FakeClassType", type);
                
            FakeClassTypeImpl.initObject(FakeClassType);
                
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
        
    }
    
    public static final PythonObject FakeClass;
    public static final PythonObject FakeStrict;

    static {
        FakeClass = null;/* FakeClassType.call(
            List.of(
                "FakeClass",
                Collections.emptyList(), 
                Collections.emptyMap() 
            ),
            Map.of("module", "magic")
        );*/
        FakeStrict = null; /*type.call(
            List.of(
                "FakeStrict",
                List.of(FakeClass),
                Collections.emptyMap()
            ),
            Map.of()
        );
        /*FakeStrict.addMethod("__new__", (args, kwargs) -> {
            PythonObject cls = (PythonObject)args.remove(0);
            PythonObject self = FakeClass.invokeStaticMethod("__new__", List.of(), Map.of());
                
            if (!args.isEmpty() || !kwargs.isEmpty())
                throw new FakeUnpicklingError(
                    String.format(
                        "%s was instantiated with unexpected arguments %s, %s",
                        cls, args, kwargs));
            return self;
        });*/
    }
    
    /*
         # comparison logic

         def __eq__(self, other):
             if not hasattr(other, "__name__"):
                 return False
             if hasattr(other, "__module__"):
                 return self.__module__ == other.__module__ and self.__name__ == other.__name__
             else:
                 return self.__module__ + "." + self.__name__ == other.__name__

         def __ne__(self, other):
             return not self == other

         def __hash__(self):
             return hash(self.__module__ + "." + self.__name__)

         def __instancecheck__(self, instance):
             return self.__subclasscheck__(instance.__class__)

         def __subclasscheck__(self, subclass):
             return (self == subclass or
                     (bool(subclass.__bases__) and
                      any(self.__subclasscheck__(base) for base in subclass.__bases__)))
         
    }*/
    /*
    class FakeStrict(FakeClass, object):
  

    def __setstate__(self, state):
        slotstate = None

        if (isinstance(state, tuple) and len(state) == 2 and
            (state[0] is None or isinstance(state[0], dict)) and
            (state[1] is None or isinstance(state[1], dict))):
            state, slotstate = state

        if state:
            # Don't have to check for slotstate here since it's either None or a dict
            if not isinstance(state, dict):
                raise FakeUnpicklingError("{0}.__setstate__() got unexpected arguments {1}".format(self.__class__, state))
            else:
                self.__dict__.update(state)

        if slotstate:
            self.__dict__.update(slotstate)
    */
    
    
    /**
     * Factory of fake classses. It will create fake class definitions on demand based on the passed
     * arguments
     */
    public static class FakeClassFactory {

        private Object default0;
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
                List /*ImmutableList*/ special_cases /*=()*/, Object default_class /*=FakeStrict*/) {
            //self.special_cases = dict( ((i.__module__, i.__name__), i) for i in special_cases)
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
            PythonObject klass;
            
            klass = this.class_cache.get(List.of(module, name));
            if(klass != null)
                return klass;

            //klass = self.special_cases.get((module, name), None)

            //if not klass:
                // generate a new class def which inherits from the default fake class
                klass = null;/*type.call(
                    List.of(
                        name, 
                        List.of(
                            this.default0
                        )
                    ), 
                    Map.of(
                        "__module__", module
                    )
                );*/

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

        public SafeUnpickler(InputStream file, FakeClassFactory class_factory/*=None*/, Object safe_modules/*=()*/,
                     boolean use_copyreg/*=False*/, String encoding/*="bytes"*/, String errors/*="strict"*/) {
            super(file, class_factory, encoding=encoding, errors=errors);
            // A set of modules which are safe to load
            /*self.safe_modules = set(safe_modules)
            self.use_copyreg = use_copyreg*/
        }
    
        @Override
        public PythonObject find_class(String module, String name) {
            
            /*if module in self.safe_modules:
                __import__(module)
                mod = sys.modules[module]
                if not hasattr(mod, "__all__") or name in mod.__all__:
                    klass = getattr(mod, name)
                    return klass
            */
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
