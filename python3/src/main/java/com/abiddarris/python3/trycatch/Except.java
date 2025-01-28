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

import com.abiddarris.python3.PythonObject;

class Except {

    private ExceptionHandler exceptionHandler;
    private PythonObject[] exceptions;

    Except(ExceptionHandler exceptionHandler, PythonObject[] exceptions) {
        this.exceptionHandler = exceptionHandler;
        this.exceptions = exceptions;
    }

    ExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }
    
    PythonObject[] getExceptions() {
        return this.exceptions;
    }

}
