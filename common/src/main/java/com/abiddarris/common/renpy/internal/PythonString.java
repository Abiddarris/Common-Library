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
package com.abiddarris.common.renpy.internal;

import static com.abiddarris.common.renpy.internal.Python.newBoolean;
import static com.abiddarris.common.renpy.internal.Python.newInt;

import static java.util.regex.Pattern.quote;

import java.util.ArrayList;
import java.util.List;

class PythonString extends PythonObject {

    private String string;

    PythonString(String string) {
        this.string = string;
    }

    private static PythonObject add(PythonString self, PythonObject value) {
        return newString(self.string + value);
    }

    private static PythonObject contains(PythonString self, PythonObject key) {
        return newBoolean(self.string.contains(key.toString()));
    }

    private static PythonObject stringHash(PythonString self) {
        return newInt(self.string.hashCode());
    }

    private static PythonObject stringEq(PythonString self, PythonObject eq) {
        if (!(eq instanceof PythonString)) {
            return False;
        }

        return newBoolean(self.string.equals(((PythonString) eq).string));
    }
    
    private static PythonObject rsplit(PythonString self, PythonObject sep, PythonObject maxsplit) {
        int jMaxSplit = maxsplit.toInt();
        if (jMaxSplit == 0) {
            return newList(self);
        }
        
        String jSep = sep.toString();
        if (jMaxSplit == -1) {
            String[] jResult = self.string.split(quote(jSep));
            List<PythonObject> result = new ArrayList<>();
            
            for(String component : jResult) {
            	result.add(newString(component));
            }
            
            return newList(result);
        }
        
        List<PythonObject> result = new ArrayList<>();
        int searchStart = self.string.length();
        int sepLength = jSep.length();
        int lastStartPos = searchStart;
        
        for(int i = 0; i < jMaxSplit; ++i) {
        	int startPos = self.string.lastIndexOf(jSep, searchStart);
            if (startPos == -1) {
                continue;
            }
            
            String component = self.string.substring(startPos + sepLength, lastStartPos);
            result.add(0, newString(component));
            
            lastStartPos = startPos;
        }
        result.add(0, newString(self.string.substring(0, lastStartPos)));
        
        return newList(result);
    }

    private static PythonObject count(PythonString self, PythonString sub) {
        int occurances = 0;
        int searchStart = 0;
        int subLen = sub.string.length();

        while (true) {
            searchStart = self.string.indexOf(sub.string, searchStart);
            if (searchStart == -1) {
                break;
            }

            searchStart += subLen;
            occurances++;
        }

        return newInt(occurances);
    }

    private static PythonObject startsWith(PythonString self, PythonObject prefix) {
        return newBoolean(self.string.startsWith(prefix.toString()));
    }

    @Override
    public String toString() {
        return string;
    }
}
