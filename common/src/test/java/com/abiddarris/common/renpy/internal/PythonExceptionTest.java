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

import static com.abiddarris.common.renpy.internal.PythonObject.Exception;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicBoolean;

public class PythonExceptionTest {
    
    @Test
    public void newException() {
        PythonObject exception = Exception.callAttribute("__new__", new PythonArgument());
        try {
            exception.raise();
            
            throw new AssertionFailedError();
        } catch (PythonException e) {
            assertEquals(exception, e.getException());
        }
    }
    
    @Test
    public void tryExceptTest() {
        AtomicBoolean catchCalled = new AtomicBoolean(false);
        PythonObject except = Exception.callAttribute("__new__", new PythonArgument());
        tryExcept(() -> {
            except.raise();
        })
        .onExcept((e -> {
            catchCalled.set(true);
                
            assertEquals(except, e);
        }), Exception)
        .execute();
        
        assertTrue(catchCalled.get());
    }
    
}
