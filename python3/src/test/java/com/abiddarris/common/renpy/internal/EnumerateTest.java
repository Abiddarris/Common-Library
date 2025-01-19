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
import static com.abiddarris.common.renpy.internal.Builtins.enumerate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class EnumerateTest {

    @Test
    public void enumerateTest() {
        List<PythonObject> expected = List.of(
                newInt(0), newInt(10),
                newInt(1), newInt(100),
                newInt(2), newInt(831)
        );
        List<PythonObject> result = new ArrayList<>();
        for (PythonObject tuple : enumerate.call(newList(newInt(10), newInt(100), newInt(831)))) {
            result.add(tuple.getItem(newInt(0)));
            result.add(tuple.getItem(newInt(1)));
        }

        assertEquals(expected, result);
    }

}
