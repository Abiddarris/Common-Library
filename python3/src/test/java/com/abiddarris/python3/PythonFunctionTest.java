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

import static com.abiddarris.common.reflect.Reflections.findMethodByName;
import static com.abiddarris.python3.PythonObject.getItem;
import static com.abiddarris.python3.PythonObject.newDict;
import static com.abiddarris.python3.PythonObject.newFunction;
import static com.abiddarris.python3.PythonObject.newInt;
import static com.abiddarris.python3.PythonObject.newString;
import static com.abiddarris.python3.PythonObject.newTuple;
import static com.abiddarris.python3.PythonObject.tryExcept;
import static com.abiddarris.python3.Builtins.TypeError;

import static org.junit.jupiter.api.Assertions.*;

import com.abiddarris.python3.signature.PythonArgument;
import com.abiddarris.python3.signature.PythonParameter;
import com.abiddarris.python3.signature.PythonSignatureBuilder;
import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;

public class PythonFunctionTest {
    
    private static boolean noParameterTestFunctionCalled;
    private static boolean arg_varPosArg_andVarKeyArg_function_called;
    private static boolean default_pos_argument_called;
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
    
    @Test
    public void arg_varPosArg_andVarKeyArg() {
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "arg_varPosArg_andVarKeyArg_function"),
            new PythonSignatureBuilder()
                .addParameter("arg1")
                .addParameter("*args")
                .addParameter("**kwargs")
                .build());
        
        function.call(new PythonArgument()
                .addPositionalArgument(newString("Cat"))
                .addPositionalArgument(newString("Dog"))
                .addPositionalArgument(newString("Wolf"))
                .addKeywordArgument("time", newString("12:00"))
                .addKeywordArgument("day_count", newString("100")));
        
        assertTrue(arg_varPosArg_andVarKeyArg_function_called);
    }
    
    @Test
    public void varPosArg_unpackTuple() {
        varPositionalParametersFunction = null;
        
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "varPositionalParametersFunction"),
            new PythonSignatureBuilder()
                .addParameter("*dict")
                .build());
        PythonObject tuple = newTuple(
            newString("Dog"), newString("Doggy"),
            newString("Puppy"));
        
        function.call(new PythonArgument()
            .addPositionalArguments(tuple));
        
        assertEquals(newString("Dog"), getItem(varPositionalParametersFunction, newInt(0)));
        assertEquals(newString("Doggy"), getItem(varPositionalParametersFunction, newInt(1)));
        assertEquals(newString("Puppy"), getItem(varPositionalParametersFunction, newInt(2)));
    }
        
    @Test
    public void varKeyArg_unpackDict() {
        varKeywordArgument = null;
        
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "varKeywordArgumentFunction"),
            new PythonSignatureBuilder()
                .addParameter("**kwargs")
                .build());
        
        PythonObject dict = newDict(newString("Day"), newString("12:00"),
            newString("Night"), newString("24:00"));
        
        function.call(new PythonArgument()
                        .addKeywordArguments(dict));
        
        assertEquals(newString("12:00"), getItem(varKeywordArgument, newString("Day")));
        assertEquals(newString("24:00"), getItem(varKeywordArgument, newString("Night")));
    }
    
    @Test
    public void tooMuchArguments() {
        PythonObject function = newFunction(
            findMethodByName(PythonFunctionTest.class, "oneParameterTestFunction"), "object");
        
        PythonObject arg = newString("Dog");
        ObjectWrapper<Boolean> onExceptCalled = new ObjectWrapper<>();
        tryExcept(() -> function.call(arg, arg))
            .onExcept(e -> onExceptCalled.setObject(true), TypeError)
            .execute();
        
        assertTrue(onExceptCalled.getObject());
    }
    
    @Test
    public void default_pos_argument() {
        PythonObject func = newFunction(PythonFunctionTest.class, "defaultPosFunction", new PythonSignatureBuilder()
            .addParameter("obj", newString("My Value"))
            .build());
        func.call();
        
        assertTrue(default_pos_argument_called);
    }
    
    public static void defaultPosFunction(PythonObject obj) {
        default_pos_argument_called = true;
        
        assertEquals(newString("My Value"), obj);
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
    
    public static void arg_varPosArg_andVarKeyArg_function(PythonObject arg1, PythonObject args, PythonObject kwargs) {
        arg_varPosArg_andVarKeyArg_function_called = true;
        
        assertEquals(newString("Cat"), arg1);
        assertEquals(newString("Dog"), args.getItem(newInt(0)));
        assertEquals(newString("Wolf"), args.getItem(newInt(1)));
        assertEquals(newString("12:00"), kwargs.getItem(newString("time")));
        assertEquals(newString("100"), kwargs.getItem(newString("day_count")));
    }
}
