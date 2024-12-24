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
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.Builtins.object;
import static com.abiddarris.common.renpy.internal.core.Functions.any;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BuiltinFunctionTest {

    @Test
    public void any_false() {
        assertEquals(False, any(newTuple(False, None, newInt(0))));
    }

    @Test
    public void any_true() {
        assertEquals(True, any(newTuple(False, object.call(), newInt(0))));
    }
}