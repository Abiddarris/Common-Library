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

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PythonListTest {

    @Test
    public void negativeIndexTest() {
        PythonObject list = newList(newInt(12), newInt(13), newInt(14));

        assertEquals(newInt(14), list.getItem(newInt(-1)));
    }

    @Test
    public void popTest() {
        PythonObject list = newList(newInt(12), newInt(13), newInt(14));

        assertEquals(newInt(14), list.callAttribute("pop"));
        assertEquals(2, list.length());
    }
}
