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

import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PythonTupleTest {
    
    @Test
    public void iter_test() {
        PythonObject tuple = newTuple(newString("I"), newString("You"), newString("We"), newString("He"));
        List<PythonObject> expected = List.of(
            newString("I"), newString("You"),
            newString("We"), newString("He")
        );
        List<PythonObject> actual = new ArrayList<>();
        for(PythonObject element : tuple) {
            actual.add(element);
        }
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void nonEmptyTuple_getLength() {
        PythonObject tuple = newTuple(newString("I"), newString("You"), newString("We"), newString("He"));
        assertEquals(4, tuple.length());
    }
     
    @Test
    public void nonEmptyTuple_toBoolean() {
        PythonObject tuple = newTuple(newString("I"), newString("You"), newString("We"), newString("He"));
        assertEquals(true, tuple.toBoolean());
    }
    
    @Test
    public void emptyTuple_toBoolean() {
    	PythonObject tuple = newTuple();
        assertEquals(false, tuple.toBoolean());
    }
}
