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
package com.abiddarris.python3.signature;

import com.abiddarris.python3.PythonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public class PythonParameter {
    
    List<PythonObject> positionalArguments = new ArrayList<>();
    Map<String, PythonObject> keywordArguments = new HashMap<>();
    
    public PythonParameter() {
    }
    
    public PythonParameter(PythonParameter parameter) {
        this.positionalArguments = new ArrayList<>(parameter.positionalArguments);
        this.keywordArguments.putAll(parameter.keywordArguments);
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
    
    public PythonParameter addKeywordArgument(String key, PythonObject argument) {
        keywordArguments.put(key, argument);
        
        return this;
    }
    
    public PythonParameter addPositionalArguments(PythonObject tuple) {
        for(PythonObject arg : tuple) {
        	addPositionalArgument(arg);
        }
        
        return this;
    }
    
    public PythonParameter addKeywordArguments(PythonObject dict) {
        for(PythonObject key : dict) {
            addKeywordArgument(key.toString(), dict.getItem(key));
    	}
        
        return this;
    }
    
    public PythonParameter addPositionalArgumentsFromArray(PythonObject... args) {
        for(PythonObject arg : args) {
        	addPositionalArgument(arg);
        }
        
        return this;
    }
}
