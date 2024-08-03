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

import static com.abiddarris.common.renpy.internal.PythonObject.newDict;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;

import com.abiddarris.common.renpy.internal.PythonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PythonSignature {
    
    private List<String> keywords;
    private Map<String, PythonObject> signature;
    
    PythonSignature(Map<String, PythonObject> signature) {
        this.signature = signature;
        
        keywords = new ArrayList<>(signature.keySet());
    }
    
    public PythonObject invoke(Method method, PythonParameter parameter) {
        PythonObject[] args = new PythonObject[signature.size()];
        List<PythonObject> posArgs = new ArrayList<>(parameter.positionalArguments);
        Map<String, PythonObject> keywordArgs = new HashMap<>(parameter.keywordArguments);
        
        if(keywords.size() == 0 && (posArgs.size() != 0 || keywordArgs.size() != 0)) {
            throw new IllegalArgumentException("takes 0 positional arguments but " + posArgs.size() + " was given");
        }
        
        for(int i = 0; i < keywords.size(); ++i) {
            String keyword = keywords.get(i);
            
            if(keyword.startsWith("**")) {
                Map<PythonObject, PythonObject> dict = new HashMap<>();
                keywordArgs.forEach((k, v) -> dict.put(newString(k), v));
                keywordArgs.clear();
                
                args[i] = newDict(dict);
                break;
            }
            
            if(keyword.startsWith("*")) {
                args[i] = newTuple(posArgs.toArray(PythonObject[]::new));
                posArgs.clear();
                
                continue;
            }
            
            PythonObject arg = posArgs.isEmpty() ? keywordArgs.remove(keyword) : posArgs.remove(0);
        	args[i] = arg;
        }
        
        try {
            return (PythonObject)method.invoke(null, (Object[])args);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}