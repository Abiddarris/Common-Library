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
package com.abiddarris.python3;

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newList;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.python3.PythonObject;

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

    @Test
    public void setItemWithNegativeIndex() {
        PythonObject list = newList(newInt(12), newInt(13), newInt(14));

        list.setItem(newInt(-1), newInt(89));

        assertEquals(newInt(89), list.getItem(newInt(2)));
    }
}
