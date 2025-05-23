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
package com.abiddarris.python3.core.functions;

import com.abiddarris.python3.PythonFunction;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.invocator.P2Invocator;
import com.abiddarris.python3.invocator.P4Invocator;
import com.abiddarris.python3.invocator.P5Invocator;
import com.abiddarris.python3.invocator.P6Invocator;
import com.abiddarris.python3.invocator.PInvocator;
import com.abiddarris.python3.invocator.V1Invocator;
import com.abiddarris.python3.invocator.V2Invocator;
import com.abiddarris.python3.invocator.V3Invocator;
import com.abiddarris.python3.invocator.V4Invocator;
import com.abiddarris.python3.invocator.V5Invocator;
import com.abiddarris.python3.signature.PythonSignature;

public class Functions {

    public static PythonObject newFunction(PFunction function, String... argumentNames) {
        return new PythonFunction(PInvocator.INSTANCE, function, PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(P4Function function, PythonSignature signature) {
        return new PythonFunction(P4Invocator.INSTANCE, function, signature);
    }

    public static PythonObject newFunction(P5Function function, PythonSignature signature) {
            return new PythonFunction(P5Invocator.INSTANCE, function, signature);
        }

    public static PythonObject newFunction(P6Function function, PythonSignature signature) {
        return new PythonFunction(P6Invocator.INSTANCE, function, signature);
    }

    public static PythonObject newFunction(V1Function function, String... argumentNames) {
        return new PythonFunction(V1Invocator.INSTANCE, function, PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(P2Function function, String... argumentNames) {
        return new PythonFunction(P2Invocator.INSTANCE, function, PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(V2Function function, String... argumentNames) {
        return new PythonFunction(V2Invocator.INSTANCE, function,  PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(V3Function function, String... argumentNames) {
        return newFunction(function, PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(V3Function function, PythonSignature signature) {
        return new PythonFunction(V3Invocator.INSTANCE, function, signature);
    }

    public static PythonObject newFunction(V4Function function, String... argumentNames) {
        return newFunction(function, PythonSignature.from(argumentNames));
    }

    public static PythonObject newFunction(V4Function function, PythonSignature signature) {
        return new PythonFunction(V4Invocator.INSTANCE, function, signature);
    }

    public static PythonObject newFunction(V5Function function, String... parameterNames) {
        if (parameterNames.length != 5) {
            throw new IllegalArgumentException("parameterNames must exactly has length of 5 (" + parameterNames.length + " given)");
        }
        return new PythonFunction(V5Invocator.INSTANCE, function, PythonSignature.from(parameterNames));
    }
}
