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
package com.abiddarris.common.renpy.internal.object;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonParameter;

public class PythonMethod extends PythonObject {
        
    private PythonObject function;
    private PythonObject self;
        
    public PythonMethod(PythonObject self, PythonObject function) {
        this.self = self;
        this.function = function;
    }
        
    @Override
    public PythonObject call(PythonParameter parameter) {
        PythonArgument newParams = new PythonArgument(parameter);
        newParams.insertPositionalArgument(0, self);
            
        return function.call(newParams);
    }
}