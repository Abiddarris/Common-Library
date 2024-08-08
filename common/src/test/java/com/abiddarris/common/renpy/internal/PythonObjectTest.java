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
import static com.abiddarris.common.renpy.internal.PythonObject.object;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
