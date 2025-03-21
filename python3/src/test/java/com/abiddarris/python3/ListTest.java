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

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newList;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Python.tryExcept;
import static com.abiddarris.python3.Builtins.IndexError;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.common.utils.ObjectWrapper;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ListTest {
    
    @Test
    public void getItem_onList() {
        PythonObject list = newList(newString("Cat"), newString("Rabbit"));
        
        assertEquals(newString("Cat"), list.getItem(newInt(0)));
    }
    
    @Test
    public void getItem_indexError() {
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>(false);
        tryExcept(() -> newList().getItem(newInt(1)))
            .onExcept(e -> thrown.setObject(true), IndexError)
            .execute();
        assertEquals(true, thrown.getObject());
    }
    
    @Test
    public void insert_onList() {
        PythonObject list = newList(newString("Chicken"));
        list.callAttribute("insert", newInt(0), newString("Wolf"));
        
        assertEquals(newString("Wolf"), list.getItem(newInt(0)));
        assertEquals(newString("Chicken"), list.getItem(newInt(1)));
    }
    
    @Test
    public void insert_outOfBounds() {
        PythonObject list = newList(newString("Chicken"));
        list.callAttribute("insert", newInt(3), newString("Wolf"));
        
        assertEquals(newString("Wolf"), list.getItem(newInt(1)));
        assertEquals(newString("Chicken"), list.getItem(newInt(0)));
    }
    
    @Test
    public void iterationTest() {
        PythonObject list = newList(newString("Cat"), newString("Rabbit"));
        List<PythonObject> actual = new ArrayList<>();
        for(PythonObject element : list) {
        	actual.add(element);
        }
        
        assertEquals(List.of(newString("Cat"), newString("Rabbit")), actual);
    }
    
}
