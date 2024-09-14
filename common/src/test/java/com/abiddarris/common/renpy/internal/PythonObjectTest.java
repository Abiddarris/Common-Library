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

import static com.abiddarris.common.renpy.internal.PythonObject.AttributeError;
import static com.abiddarris.common.renpy.internal.PythonObject.newClass;
import static com.abiddarris.common.renpy.internal.PythonObject.newDict;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.PythonObject.str;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;

public class PythonObjectTest {

    @Test
    public void object_toBoolean() {
        assertEquals(true, object.toBoolean());
    }

    @Test
    public void object_createNewInstance() {
        PythonObject obj = object.call(new PythonArgument());
    }

    @Test
    public void object_getNonExistAttribute() {
        PythonObject obj = object.call(new PythonArgument());
        ObjectWrapper<Boolean> thrown = new ObjectWrapper<>();
        tryExcept(() -> {
            obj.getAttribute("c");
        }).onExcept((e) -> {
            thrown.setObject(true);
        }, AttributeError).execute();

        assertTrue(thrown.getObject());
    }
    
    @Test
    public void object_type() {
        assertEquals(type, type.call(object));
    }
    
    @Test
    public void objectInstance_type() {
        assertEquals(object, type.call(object.call()));
    }
    
    @Test
    public void objectInstance_toString() {
        PythonObject instance = object.call();
        assertEquals("<object object>", instance.toString());
    }
    
    @Test
    public void newClass_withAttributes() {
        PythonObject TestClass = newClass("TestClass", newTuple(), newDict(
                newString("number"), newInt(42)));
        assertEquals(42, TestClass.getAttribute("number").toInt());
    }
    
    @Test
    public void notEqualsTest_onEqualsObject() {
        PythonObject object1 = object.call();
        
        assertFalse(object1.jNotEquals(object1));
    }
    
    @Test
    public void notEqualsTest_onNotEqualsObject() {
        PythonObject object1 = object.call();
        PythonObject object2 = object.call();
        
        assertTrue(object1.jNotEquals(object2));
    }

    @Test
    public void testSplitOnExistSep() {
        PythonObject string = newString("java.util.ArrayList");
        PythonObject splitedString = string.callAttribute("rsplit", newString("."), newInt(1));

        assertEquals(newString("java.util"), splitedString.getItem(newInt(0)));
        assertEquals(newString("ArrayList"), splitedString.getItem(newInt(1)));
    }

}
