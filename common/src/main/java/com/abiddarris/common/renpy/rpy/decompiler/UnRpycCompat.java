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
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.core.Attributes.callNestedAttribute;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

public class UnRpycCompat {

    static void initLoader() {
        registerLoader("decompiler.unrpyccompat", (unrpyccompat) -> {
            FakeModuleSubclassCheckGeneratorImpl.define(unrpyccompat);
            DecompilerBaseAdvanceToLineGeneratorImpl.define(unrpyccompat);
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
}
