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

import com.abiddarris.common.renpy.internal.PythonObject;
import java.util.ArrayList;
import java.util.List;

public class PythonParameter {
    
    List<PythonObject> positionalArguments = new ArrayList<>();
    
    public PythonParameter() {
    }
    
    public PythonParameter(PythonParameter parameter) {
        this.positionalArguments = new ArrayList<>(parameter.positionalArguments);
    }
    
    public PythonParameter addPositionalArgument(PythonObject argument) {
        positionalArguments.add(argument);
        
        return this;
    }
    
    public PythonParameter insertPositionalArgument(int index, PythonObject argument) {
        if(index > positionalArguments.size()) {
            throw new IllegalArgumentException("Cannot insert to index greater than positional argument size!");
        }
        positionalArguments.add(index, argument);
        
        return this;
    }
    
}
