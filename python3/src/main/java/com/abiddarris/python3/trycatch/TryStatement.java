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
package com.abiddarris.python3.trycatch;

import com.abiddarris.python3.PythonException;
import com.abiddarris.python3.PythonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class TryStatement {

    private Runnable tryStatement;
    private List<Except> exceptionStatements = new ArrayList<>();
    private Runnable defaultExceptionStatement;
    private Runnable finallyStatement;
    private Runnable elseStatement;

    TryStatement(Runnable tryStatement) {
        this.tryStatement = tryStatement;
    }

    void addExceptStatement(ExceptionHandler handler, PythonObject... exceptionsType) {
        exceptionStatements.add(new Except(handler, exceptionsType));
    }

    void setDefaultExceptionStatement(Runnable defaultExceptionStatement) {
        this.defaultExceptionStatement = defaultExceptionStatement;
    }

    void setElseStatement(Runnable statement) {
        this.elseStatement = statement;
    }

    void setFinallyStatement(Runnable finallyStatement) {
        this.finallyStatement = finallyStatement;
    }

    void execute() {
        try {
            try {
                tryStatement.run();
            } catch(PythonException e) {
                PythonObject exception = e.getException();
                PythonObject exceptionClass = exception.getAttribute("__class__");

                for (Except except : exceptionStatements) {
                    if(Stream.of(except.getExceptions())
                            .anyMatch(except0 -> except0 == exceptionClass)) {
                        except.getExceptionHandler()
                                .accept(exception);
                        return;
                    }
                }

                if (defaultExceptionStatement != null) {
                    defaultExceptionStatement.run();

                    return;
                }
                throw e;
            }

            if (elseStatement != null) {
                elseStatement.run();
            }
        } finally {
             if (finallyStatement != null) {
                 finallyStatement.run();
             }
        }
    }
}
