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

import static com.abiddarris.python3.Builtins.function;

import com.abiddarris.python3.invocator.Invocator;
import com.abiddarris.python3.invocator.MethodInvocator;
import com.abiddarris.python3.signature.PythonParameter;
import com.abiddarris.python3.signature.PythonSignature;

import java.lang.reflect.Method;

public class PythonFunction extends PythonObject {

    private Object target;
    private Invocator invocator;
    private PythonSignature signature;

    public PythonFunction(Method method, PythonSignature signature) {
        this(MethodInvocator.INSTANCE, method, signature);
    }

    public PythonFunction(Invocator invocator, Object target, PythonSignature signature) {
        super(function);

        invocator.validateTarget(target, signature);

        this.invocator = invocator;
        this.signature = signature;
        this.target = target;
    }

    @Override
    public PythonObject call(PythonParameter parameter) {
        PythonObject[] args = signature.parseArguments(parameter);
        PythonObject object = invocator.invoke(target, args);

        return object != null ? object : Builtins.None;
    }

}
