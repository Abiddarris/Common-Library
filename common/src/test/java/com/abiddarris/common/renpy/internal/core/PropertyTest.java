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
package com.abiddarris.common.renpy.internal.core;

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.__import__;
import static com.abiddarris.common.renpy.internal.PythonObject.property;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

import org.junit.jupiter.api.Test;

class PropertyTest {

    private static PythonObject propertytest;

    static {
        registerLoader("propertytest", (propertytest) -> {
            PropertyTest.propertytest = propertytest;

            StudentImpl.define();
        });
        __import__.call(newString("propertytest"));
    }

    private static class StudentImpl {

        private static PythonObject define() {
            ClassDefiner definer = propertytest.defineClass("Student");
            definer.defineFunction("age", property, StudentImpl.class, "age", "self");

            return definer.define();
        }

        private static PythonObject age(PythonObject self) {
            return newInt(20);
        }

    }

    @Test
    void property() {
        PythonObject student = propertytest.callAttribute("Student");
        PythonObject age = student.getAttribute("age");

        assertEquals(newInt(20), age);
    }
}