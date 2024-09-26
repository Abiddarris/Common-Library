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

import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.Builtins.type;
import static com.abiddarris.common.renpy.internal.Builtins.TypeError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;

public class NoneTest {
    
    @Test
    public void createNewNone() {
        assertEquals(None, type.call(None).call());
    }
    
    @Test
    public void callNoneTest() {
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>(false);
        tryExcept(() -> None.call())
            .onExcept((e) -> thrown.setObject(true), TypeError)
            .execute();
        
        if(!thrown.getObject()) {
            throw new AssertionError();
        }
    }
    
    @Test
    public void noneToBoolean() {
        assertFalse(None.toBoolean());
    }
    
}
