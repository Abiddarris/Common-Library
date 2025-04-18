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
 *************************************************************************************/
package com.abiddarris.unrpyc.decompiler;

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.core.Attributes.callNestedAttribute;
import static com.abiddarris.python3.core.Attributes.getNestedAttribute;
import static com.abiddarris.python3.core.Functions.isinstance;
import static com.abiddarris.python3.core.Slice.newSlice;
import static com.abiddarris.python3.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;

public class UnRpycCompat {

    private static PythonObject unrpyccompat;

    static void initLoader() {
        registerLoader("decompiler.unrpyccompat", (unrpyccompat) -> {
            UnRpycCompat.unrpyccompat = unrpyccompat;

            FakeModuleSubclassCheckGeneratorImpl.define(unrpyccompat);
            DecompilerBaseAdvanceToLineGeneratorImpl.define(unrpyccompat);
            DispatcherCallClosureImpl.define();
            DecompilerPrintInitImpl.define();
            DecompilerPrintInit1Impl.define();
        });
    }

    private static class FakeModuleSubclassCheckGeneratorImpl {

        private static PythonObject define(PythonObject unrpyccompat) {
            ClassDefiner definer = unrpyccompat.defineClass("FakeModuleSubclassCheckGenerator");
            definer.defineFunction("__init__", FakeModuleSubclassCheckGeneratorImpl.class, "init", "self", "iterable", "subclass");
            definer.defineFunction("__iter__", FakeModuleSubclassCheckGeneratorImpl.class, "iter", "self");
            definer.defineFunction("__next__", FakeModuleSubclassCheckGeneratorImpl.class, "next", "self");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject iterable, PythonObject self0) {
            self.setAttribute("iterable", iterable.callAttribute("__iter__"));
            self.setAttribute("__self__", self0);
        }

        private static PythonObject iter(PythonObject self) {
            return self;
        }

        private static PythonObject next(PythonObject self) {
            PythonObject base = callNestedAttribute(self, "iterable.__next__");
            return callNestedAttribute(self, "__self__.__subclasscheck__", base);
        }
    }

    private static class DecompilerBaseAdvanceToLineGeneratorImpl {

        private static PythonObject define(PythonObject unrpyccompat) {
            ClassDefiner definer = unrpyccompat.defineClass("DecompilerBaseAdvanceToLineGenerator");
            definer.defineFunction("__init__", DecompilerBaseAdvanceToLineGeneratorImpl.class, "init", "self", "iterable", "linenumber");
            definer.defineFunction("__iter__", DecompilerBaseAdvanceToLineGeneratorImpl.class, "iter", "self");
            definer.defineFunction("__next__", DecompilerBaseAdvanceToLineGeneratorImpl.class, "next", "self");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject iterable, PythonObject linenumber) {
            self.setAttribute("iterable", iterable.callAttribute("__iter__"));
            self.setAttribute("linenumber", linenumber);
        }

        private static PythonObject iter(PythonObject self) {
            return self;
        }

        private static PythonObject next(PythonObject self) {
            PythonObject linenumber = self.getAttribute("linenumber");
            while (true) {
                PythonObject m = callNestedAttribute(self, "iterable.__next__");
                if (m.call(linenumber).toBoolean()) {
                    return m;
                }
            }
        }
    }

    private static class DispatcherCallClosureImpl {

        private static PythonObject define() {
            ClassDefiner definer = unrpyccompat.defineClass("DispatcherCallClosure");
            definer.defineFunction("__init__", DispatcherCallClosureImpl.class, "init", "self", "dict", "name");
            definer.defineFunction("__call__", DispatcherCallClosureImpl.class, "call", "self", "func");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject dict, PythonObject name) {
            self.setAttribute("dict", dict);
            self.setAttribute("name", name);
        }

        private static PythonObject call(PythonObject self, PythonObject func) {
            self.getAttribute("dict").setItem(self.getAttribute("name"), func);

            return func;
        }

    }

    private static class DecompilerPrintInitImpl {

        private static PythonObject define() {
            ClassDefiner definer = unrpyccompat.defineClass("DecompilerPrintInit");
            definer.defineFunction("__init__", DecompilerPrintInitImpl.class, "init", "self", "renpy", "ast");
            definer.defineFunction("__iter__", DecompilerPrintInitImpl.class, "iter", "self");
            definer.defineFunction("__next__", DecompilerPrintInitImpl.class, "next", "self");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject renpy, PythonObject ast) {
            self.setAttribute("renpy", renpy);
            self.setAttribute("ast", ast);
        }

        private static PythonObject iter(PythonObject self) {
            self.setAttribute("iterable", callNestedAttribute(self, "ast.block.__iter__"));

            return self;
        }

        private static PythonObject next(PythonObject self) {
            PythonObject i = callNestedAttribute(self, "iterable.__next__");

            return isinstance(i, getNestedAttribute(self, "renpy.ast.TranslateString"));
        }
    }

    private static class DecompilerPrintInit1Impl {

        private static PythonObject define() {
            ClassDefiner definer = unrpyccompat.defineClass("DecompilerPrintInit1");
            definer.defineFunction("__init__", DecompilerPrintInit1Impl.class, "init", "self", "ast");
            definer.defineFunction("__iter__", DecompilerPrintInit1Impl.class, "iter", "self");
            definer.defineFunction("__next__", DecompilerPrintInit1Impl.class, "next", "self");

            return definer.define();
        }

        private static void init(PythonObject self, PythonObject ast) {
            self.setAttribute("ast", ast);
        }

        private static PythonObject iter(PythonObject self) {
            self.setAttribute("iterable", getNestedAttribute(self, "ast.block")
                    .getItem(newSlice(1))
                    .callAttribute("__iter__"));

            return self;
        }

        private static PythonObject next(PythonObject self) {
            PythonObject i = callNestedAttribute(self, "iterable.__next__");

            return i.getAttribute("language").pEquals(
                    getNestedAttribute(self, "ast.block")
                            .getItem(newInt(0))
                            .getAttribute("language"));
        }
    }
}
