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
 ***********************************************************************************/
package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.imp.Imports.importModule;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;
import static com.abiddarris.common.renpy.internal.PythonObject.True;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

import org.junit.jupiter.api.Test;

public class WithTest {

    private static PythonObject ContextManager;

    static {
        registerLoader("withtest", (withtest) -> {
            ContextManager = ContextManagerImpl.define(withtest);
        });

        importModule("withtest");
    }

    private static class ContextManagerImpl {

        private static PythonObject define(PythonObject withtest) {
            ClassDefiner definer = withtest.defineClass("ContextManager");
            definer.defineFunction("__init__", ContextManagerImpl.class, "init", "self");
            definer.defineFunction("__enter__", ContextManagerImpl.class, "enter", "self");
            definer.defineFunction("__exit__", ContextManagerImpl.class, "exit", "self", "v1", "v2", "v3");

            return definer.define();
        }

        private static void init(PythonObject self) {
            self.setAttribute("enter_called", False);
            self.setAttribute("exit_called", False);
        }

        private static void enter(PythonObject self) {
            self.setAttribute("enter_called", True);
        }

        private static void exit(PythonObject self, PythonObject v1, PythonObject v2, PythonObject v3) {
            self.setAttribute("exit_called", True);
        }
    }

    @Test
    public void withTest() {
        PythonObject context_manager = ContextManager.call();
        with(context_manager, () -> {
            assertEquals(True, context_manager.getAttribute("enter_called"));
        });
        assertEquals(True, context_manager.getAttribute("exit_called"));
    }
}
