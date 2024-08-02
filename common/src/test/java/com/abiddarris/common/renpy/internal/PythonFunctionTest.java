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
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.getItem;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;

import static org.junit.jupiter.api.Assertions.*;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

import org.junit.jupiter.api.Test;

public class PythonFunctionTest {
    
    private static boolean noParameterTestFunctionCalled;
    private static PythonObject oneParameterTestFunctionResult;
    private static PythonObject varPositionalParametersFunction;
    private static PythonObject varKeywordArgument;
    
    @Test
    public void noParameterTest() {
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "noParameterTestFunction"),
            new PythonSignatureBuilder().build());
        function.call(new PythonParameter());
        
        assertTrue(noParameterTestFunctionCalled);
    }
    
    @Test
    public void oneParameterTest() {
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "oneParameterTestFunction"),
            new PythonSignatureBuilder()
                .addParameter("object")
                .build());
        
        PythonObject arg = newString("Dog");
        
        function.call(new PythonArgument()
            .addPositionalArgument(arg));
        
        assertEquals(arg, oneParameterTestFunctionResult);
    }
    
    @Test
    public void oneParameterUseKeywordArgumentTest() {
        oneParameterTestFunctionResult = null;
        
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "oneParameterTestFunction"),
            new PythonSignatureBuilder()
                .addParameter("object")
                .build());
        
        PythonObject arg = newString("Dog");
        
        function.call(new PythonArgument()
            .addKeywordArgument("object", arg));
        
        assertEquals(arg, oneParameterTestFunctionResult);
    }
    
    @Test
    public void parameters() {
        varPositionalParametersFunction = null;
        
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "varPositionalParametersFunction"),
            new PythonSignatureBuilder()
                .addParameter("*dict")
                .build());
        
        PythonObject arg = newString("Dog");
        PythonObject arg1 = newString("Doggy");
        PythonObject arg2 = newString("Puppy");
        
        function.call(new PythonArgument()
            .addPositionalArgument(arg)
            .addPositionalArgument(arg1)
            .addPositionalArgument(arg2));
        
        assertEquals(arg, getItem(varPositionalParametersFunction, newInt(0)));
        assertEquals(arg1, getItem(varPositionalParametersFunction, newInt(1)));
        assertEquals(arg2, getItem(varPositionalParametersFunction, newInt(2)));
    }
    
    @Test
    public void varKeywordArgumentTest() {
        varKeywordArgument = null;
        
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "varKeywordArgumentFunction"),
            new PythonSignatureBuilder()
                .addParameter("**kwargs")
                .build());
        
        PythonObject val1 = newString("12:00");
        PythonObject val2 = newString("24:00");
        
        function.call(new PythonArgument()
                        .addKeywordArgument("Day", val1)
                        .addKeywordArgument("Night", val2));
        
        assertEquals(val1, getItem(varKeywordArgument, newString("Day")));
        assertEquals(val2, getItem(varKeywordArgument, newString("Night")));
    }
    
    public static void noParameterTestFunction() {
        noParameterTestFunctionCalled = true;
    }
    
    public static void oneParameterTestFunction(PythonObject object) {
        oneParameterTestFunctionResult = object;
    }
    
    public static void varPositionalParametersFunction(PythonObject dict) {
        varPositionalParametersFunction = dict;
    }
    
    public static void varKeywordArgumentFunction(PythonObject dict) {
        varKeywordArgument = dict;
    }
}
