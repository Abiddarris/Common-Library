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

import static com.abiddarris.common.reflect.Reflections.findMethodByName;
import static com.abiddarris.common.renpy.internal.PythonObject.newFunction;

import static org.junit.jupiter.api.Assertions.*;

import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

import org.junit.jupiter.api.Test;

public class PythonFunctionTest {
    
    private static boolean noParameterTestFunctionCalled;
    
    @Test
    public void noParameterTest() {
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "noParameterTestFunction"),
            new PythonSignatureBuilder().build());
        function.call(new PythonParameter());
        
        assertTrue(noParameterTestFunctionCalled);
    }
    
    public static void noParameterTestFunction() {
        noParameterTestFunctionCalled = true;
    }
    
}