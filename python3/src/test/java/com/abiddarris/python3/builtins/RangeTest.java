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
package com.abiddarris.python3.builtins;

import static com.abiddarris.python3.Builtins.range;
import static com.abiddarris.python3.Python.newInt;

import com.abiddarris.python3.PythonObject;

import org.junit.jupiter.api.Test;

public class RangeTest {

    @Test
    public void negativeStepTest() {
        for (PythonObject a : range.call(newInt(-10), newInt(1), newInt(2))) {
            System.out.println(a);
        }
    }

}
