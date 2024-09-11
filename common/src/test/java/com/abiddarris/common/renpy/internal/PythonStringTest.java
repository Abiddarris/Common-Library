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
import static com.abiddarris.common.renpy.internal.PythonObject.newPythonString;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PythonStringTest {
    
    @Test
    public void pyString_equalsToJavaString() {
        PythonObject home = newPythonString("Home");
        
        assertEquals(home.toString(), "Home");
    }
    
    @Test
    public void isHashCodeEquals() {
        PythonObject dog = newString("Dog");
        PythonObject dog2 = newString("Dog");
        
        assertEquals(dog.hashCode(), dog2.hashCode());
    }
    
    @Test
    public void equals_toSameString() {
    	PythonObject dog = newString("Dog");
        PythonObject dog2 = newString("Dog");
        
        assertEquals(dog, dog2);
    }
    
    @Test
    public void inTestTrue() {
        PythonObject text = newString("This is a text.");
        
        assertTrue(text.jin(newString("is")));
    }

    @Test
    public void testSplitOnExistSep() {
        PythonObject string = newString("java.util.ArrayList");
        PythonObject splitedString = string.callAttribute("rsplit", newString("."), newInt(1));

        assertEquals(newString("java.util"), splitedString.getItem(newInt(0)));
        assertEquals(newString("ArrayList"), splitedString.getItem(newInt(1)));
    }

    @Test
    public void testSplitNotExistSep() {
        PythonObject string = newString("ArrayList");
        PythonObject splitedString = string.callAttribute("rsplit", newString("."), newInt(1));

        assertEquals(newString("ArrayList"), splitedString.getItem(newInt(0)));
    }

    @Test
    public void countOneChar() {
        PythonObject string = newString("java.util.ArrayList");
        PythonObject count = string.callAttribute("count", newString("."));

        assertEquals(newInt(2), count);
    }
}
