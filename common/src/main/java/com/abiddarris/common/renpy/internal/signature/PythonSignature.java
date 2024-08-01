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

import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;

import com.abiddarris.common.renpy.internal.PythonObject;
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
        Map<String, PythonObject> arguments = new LinkedHashMap<>();
        List<PythonObject> positionalArgument = parameter.positionalArguments;
        Map<String, PythonObject> keywordArguments = parameter.keywordArguments;
        
        if(keywords.size() == 0 && (positionalArgument.size() != 0 || keywordArguments.size() != 0)) {
            throw new IllegalArgumentException("takes 0 positional arguments but " + positionalArgument.size() + " was given");
        }
        
        boolean searchInKeywordArguments = false;
        for(int i = 0; i < keywords.size(); ++i) {
            String keyword = keywords.get(i);
            if(i == positionalArgument.size()) {
                searchInKeywordArguments = true;
            }
            
            if(keyword.startsWith("*")) {
                arguments.put(keyword, newTuple(positionalArgument.subList(i, positionalArgument.size())
                        .toArray(PythonObject[]::new)));
                searchInKeywordArguments = true;
                break;
            }
            PythonObject arg = searchInKeywordArguments ? keywordArguments.get(keyword) : positionalArgument.get(i);
        	arguments.put(keyword, arg);
        }
        
        try {
            return (PythonObject)method.invoke(null, arguments.values().toArray(Object[]::new));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
