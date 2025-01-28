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

import static com.abiddarris.python3.Builtins.builtins;
import static com.abiddarris.python3.Python.newDict;
import static com.abiddarris.python3.core.functions.Functions.newFunction;
import static com.abiddarris.python3.Python.newInt;

import com.abiddarris.python3.PythonObject;

import org.junit.jupiter.api.Test;

public class MaxTest {

    @Test
    void maxTest() {
        PythonObject dict = newDict(
                newInt(1), newInt(2),
                newInt(-1), newInt(8),
                newInt(3), newInt(2),
                newInt(-5), newInt(9)
        );
        PythonObject customFunction = newFunction(key -> {
            System.out.println(key);
            return dict.callAttribute("get", key);
        }, "key");

        System.out.println(builtins.callAttribute("max", dict, customFunction));
    }

    @Test
    void ac() {
        System.out.println(findPrimeNumber(-23));

    }

    private static long findPrimeNumber(long from) {
        boolean negative;
        if (negative = from < 0) {
            from = -from;
        }

        if (from >= 0 && from < 2) {
            return 2;
        }

        while(true) {
            from++;

            boolean prime = true;
            for (long divider = 2; divider < from; divider++) {
                if (from % divider == 0) {
                    prime = false;
                    break;
                }
            }

            if (prime) {
                return negative ? -from : from;
            }
        }
    }
}
