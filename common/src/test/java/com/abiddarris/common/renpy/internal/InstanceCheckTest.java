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

import static com.abiddarris.common.renpy.internal.Python.tryExcept;
import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.Builtins.TypeError;
import static com.abiddarris.common.renpy.internal.Builtins.bool;
import static com.abiddarris.common.renpy.internal.Builtins.dict;
import static com.abiddarris.common.renpy.internal.Builtins.int0;
import static com.abiddarris.common.renpy.internal.Builtins.isinstance;
import static com.abiddarris.common.renpy.internal.Builtins.list;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;

public class InstanceCheckTest {
    
    @Test
    public void sameClassInstance() {
        assertTrue(isinstance.call(True, bool).toBoolean());
    }
    
    @Test
    public void differentClassInstance() {
        assertFalse(isinstance.call(True, list).toBoolean());
    }
    
    @Test
    public void parentClassInstance() {
        assertTrue(isinstance.call(True, int0).toBoolean());
    }
    
    @Test
    public void tupleClassInstance() {
        assertTrue(isinstance.call(True, newTuple(list, int0)).toBoolean());
    }
    
    @Test
    public void tupleClassInstance_noMatching() {
        assertFalse(isinstance.call(True, newTuple(list, dict)).toBoolean());
    }
    
    @Test
    public void passClsWithNonClass() {
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>(false);

        tryExcept(() -> isinstance.call(True, False)).
        onExcept((e) -> thrown.setObject(true), TypeError).execute();
        
        assertTrue(thrown.getObject());
    }
    
    @Test
    public void tupleClassInstance_containsNonClassObj() {
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>(false);

        tryExcept(() -> isinstance.call(True, newTuple(list, False))).
        onExcept((e) -> thrown.setObject(true), TypeError).execute();
        
        assertTrue(thrown.getObject());
    }
}
