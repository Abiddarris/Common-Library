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
package com.abiddarris.python3.invocator;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.core.functions.V3Function;
import com.abiddarris.python3.signature.PythonSignature;

public class V3Invocator implements Invocator {

    public static final V3Invocator INSTANCE = new V3Invocator();

    private V3Invocator() {}

    @Override
    public PythonObject invoke(Object target, PythonObject[] args) {
        ((V3Function)target).execute(args[0], args[1], args[2]);

        return null;
    }

    @Override
    public void validateTarget(Object target, PythonSignature signature) {
        if (!(target instanceof V3Function)) {
            throwInvalidTargetException(target);
        }
    }
}
