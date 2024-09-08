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

import static com.abiddarris.common.renpy.internal.Python.newFunction;
import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.core.Attributes.callNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Attributes.getNestedAttribute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AttributesTest {

    @Test
    public void nestedAttributeTest() {
        PythonObject middle = object.call();
        middle.setAttribute("secret", newInt(3));

        PythonObject root = object.call();
        root.setAttribute("middle_section", middle);

        assertEquals(newInt(3), getNestedAttribute(root, "middle_section.secret"));
    }

    @Test
    public void callNestedAttributeTest() {
        PythonObject middle = object.call();
        middle.setAttribute("change_value", newFunction(AttributesTest.class, "changeValue", "target"));

        PythonObject root = object.call();
        root.setAttribute("middle_section", middle);

        PythonObject target = object.call();
        target.setAttribute("val", False);

        assertEquals(newString("I'm called"), callNestedAttribute(root, "middle_section.change_value", target));
        assertEquals(True, target.getAttribute("val"));
    }

    private static PythonObject changeValue(PythonObject target) {
        target.setAttribute("val", True);

        return newString("I'm called");
    }

}
