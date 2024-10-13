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
package com.abiddarris.common.renpy.internal.signature;

import static com.abiddarris.common.renpy.internal.Python.newInt;

import com.abiddarris.common.renpy.internal.PythonObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class PythonSignatureBuilder {
    
    private boolean canAdd = true;
    private String kwargsName;
    private Map<String, PythonObject> signature = new LinkedHashMap<>();
    
    public PythonSignatureBuilder() {
    }
    
    public PythonSignatureBuilder(String... args) {
        for(var arg : args) {
        	addParameter(arg);
        }
    }
    
    public PythonSignatureBuilder addParameter(String name) {
        return addParameter(name, null);
    }

    public PythonSignatureBuilder addParameter(String name, PythonObject parameter) {
        if(!canAdd) {
            throw new IllegalStateException("Unable to add new parameter after " + kwargsName);
        }

        if(name.startsWith("**")) {
            canAdd = false;
            kwargsName = name;
        }

        signature.put(name, parameter);

        return this;
    }

    public PythonSignatureBuilder addParameter(String name, long defaultValue) {
        return addParameter(name, newInt(defaultValue));
    }

    public PythonSignature build() {
        return new PythonSignature(
            new LinkedHashMap<>(signature)
        );
    }
}
