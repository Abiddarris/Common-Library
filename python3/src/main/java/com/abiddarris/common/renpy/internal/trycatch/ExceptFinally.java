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
package com.abiddarris.common.renpy.internal.trycatch;

import com.abiddarris.common.renpy.internal.PythonObject;

public class ExceptFinally {

    private TryStatement statement;
    private StateValidator validator = new StateValidator();

    public ExceptFinally(Runnable tryRunnable) {
        statement = new TryStatement(tryRunnable);
    }
    
    public ExceptFinallyElseExecutable onExcept(ExceptionHandler handler, PythonObject... exceptionsType) {
        validator.checkValid();
        validator.invalidate();

        statement.addExceptStatement(handler, exceptionsType);
        
        return new ExceptFinallyElseExecutable(statement);
    }

    public ElseFinallyExecutable onExcept(Runnable statement) {
        validator.checkValid();
        validator.invalidate();

        this.statement.setDefaultExceptionStatement(statement);

        return new ElseFinallyExecutable(this.statement);
    }

    public void onFinally(Runnable statement) {
        validator.checkValid();
        validator.invalidate();

        this.statement.setFinallyStatement(statement);
        this.statement.execute();
    }
}
