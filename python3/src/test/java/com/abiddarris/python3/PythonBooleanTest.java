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

import static com.abiddarris.python3.Builtins.False;
import static com.abiddarris.python3.Builtins.True;
import static com.abiddarris.python3.Builtins.bool;
import static com.abiddarris.python3.PythonObject.newBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.python3.signature.PythonArgument;

import org.junit.jupiter.api.Test;

public class PythonBooleanTest {
    
    @Test
    public void newBoolean_fromFalse_directBoolCall() {
        PythonObject result = bool.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(bool)
            .addPositionalArgument(False));
        
        assertEquals(False, result);
    }
    
    @Test
    public void newBoolean_fromTrue_directBoolCall() {
        PythonObject result = bool.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(bool)
            .addPositionalArgument(True));
        
        assertEquals(True, result);
    }
    
    @Test
    public void newBoolean_indirect() {
        assertEquals(False, newBoolean(false));
        assertEquals(True, newBoolean(true));
    }
    
    @Test
    public void boolean_toBoolean() {
        assertEquals(false, False.toBoolean());
        assertEquals(true, True.toBoolean());
    }
}
