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

import static com.abiddarris.common.renpy.internal.Builtins.AttributeError;
import static com.abiddarris.common.renpy.internal.Builtins.Exception;
import static com.abiddarris.common.renpy.internal.Builtins.ValueError;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.concurrent.atomic.AtomicBoolean;

public class PythonExceptionTest {
    
    @Test
    public void newException() {
        PythonObject exception = Exception.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(Exception));
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
        PythonObject except = Exception.callAttribute("__new__", new PythonArgument()
            .addPositionalArgument(Exception));
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

    @Test
    public void elseStatementExecutedTest() {
        ObjectWrapper<Boolean> elseStatementCalled = new ObjectWrapper<>(false);

        tryExcept(() -> {})
                .onExcept((e) -> {}, AttributeError)
                .onElse(() -> elseStatementCalled.setObject(true))
                .execute();

        assertTrue(elseStatementCalled.getObject());
    }

    @Test
    public void elseStatementOnErrorThrownTest() {
        ObjectWrapper<Boolean> elseStatementCalled = new ObjectWrapper<>(false);

        tryExcept(() -> AttributeError.call().raise())
                .onExcept((e) -> {}, AttributeError)
                .onElse(() -> elseStatementCalled.setObject(true))
                .execute();

        assertFalse(elseStatementCalled.getObject());
    }

    @Test
    public void defaultExceptionTest() {
        ObjectWrapper<Boolean> elseStatementCalled = new ObjectWrapper<>(false);

        tryExcept(() -> AttributeError.call().raise())
                .onExcept(() -> elseStatementCalled.setObject(true))
                .execute();

        assertTrue(elseStatementCalled.getObject());
    }

    @Test
    public void finallyTestWhenExceptionIsThrown() {
        ObjectWrapper<Boolean> finallyCalled = new ObjectWrapper<>(false);
        ObjectWrapper<Boolean> exceptionThrown = new ObjectWrapper<>(false);
        tryExcept(() -> {
            tryExcept(() -> ValueError.call().raise()).onFinally(() -> finallyCalled.setObject(true));
        }).onExcept((e) -> exceptionThrown.setObject(true), ValueError).execute();

        assertTrue(finallyCalled.getObject());
        assertTrue(exceptionThrown.getObject());
    }

    @Test
    public void finallyTestWhenNothingIsThrown() {
        ObjectWrapper<Boolean> finallyCalled = new ObjectWrapper<>(false);

        tryExcept(() -> {})
                .onFinally(() -> finallyCalled.setObject(true));

        assertTrue(finallyCalled.getObject());
    }
}
