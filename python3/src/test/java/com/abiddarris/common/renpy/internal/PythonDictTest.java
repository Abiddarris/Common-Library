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
import static com.abiddarris.common.renpy.internal.Builtins.KeyError;
import static com.abiddarris.common.renpy.internal.PythonObject.newDict;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PythonDictTest {
    
    @Test
    public void dict_iter() {
        PythonObject dict = newDict(
            newString("Morning"), newString("06:00"),
            newString("Noon"), newString("12:00"),
            newString("Afternoon"), newString("18:00")
        );
        List<PythonObject> expected = List.of(
            newString("Morning"), newString("Noon"), newString("Afternoon")
        );
        List<PythonObject> actual = new ArrayList<>();
        for(PythonObject key : dict) {
        	actual.add(key);
        }
        
        assertEquals(expected, actual);
    }
    
    @Test
    public void dict_setItem() {
        PythonObject dict = newDict(newString("T"), newString("D"));
        dict.setItem(newString("T"), newString("I"));
        
        assertEquals(newString("I"), dict.getItem(newString("T")));
    }
    
    @Test
    public void nonExistKey() {
        ObjectWrapper<Boolean> called = new ObjectWrapper<>(false);
        tryExcept(() -> newDict().getItem(newString("NotExist"))).
        onExcept((e) -> called.setObject(true), KeyError).
        execute();
        
        assertEquals(true, called.getObject());
    }
}
