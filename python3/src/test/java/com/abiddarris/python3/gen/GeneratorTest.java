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
package com.abiddarris.python3.gen;

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.gen.Generators.newGenerator;

import com.abiddarris.python3.PythonObject;

import org.junit.jupiter.api.Test;

public class GeneratorTest {

    @Test
    void generator_test() {
        PythonObject x = newTuple(newInt(1), newInt(2), newInt(3), newInt(4));
        PythonObject y = newTuple(newInt(20), newInt(21), newInt(22));
        PythonObject z = newTuple(newInt(1), newInt(2));

        PythonObject gen = newGenerator().forEach((vars) -> x)
                .name((vars, obj) -> vars.put("b", obj))
                .filter((vars) -> vars.get("b").notEquals(newInt(2)))
                .filter((vars) -> vars.get("b").notEquals(newInt(3)))
                .forEach((vars) -> y)
                .name((vars, obj) -> vars.put("c", obj))
                .filter((vars) -> vars.get("c").notEquals(newInt(21)))
                .forEach(vars -> z)
                .name((vars, object) -> vars.put("d", object))
                .yield((vars) -> newTuple(
                        vars.get("b"), vars.get("c"), vars.get("d")
                ));

        for (PythonObject e : gen) {
            System.out.println(e);
        }
    }

}
