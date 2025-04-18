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

import static com.abiddarris.python3.Builtins.None;
import static com.abiddarris.python3.Builtins.str;
import static java.util.regex.Pattern.quote;

import java.util.ArrayList;
import java.util.List;

class PythonString extends PythonObject {

    public static void init2() {
        str.defineAttribute("__module__", newString("builtins"));
        str.defineFunction("strip", PythonString::strip, "self");
    }

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
            return Builtins.False;
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

    private static PythonObject multiply(PythonString self, PythonInt value) {
        int repeat = Math.max(value.toInt(), 0);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < repeat; i++) {
            builder.append(self.string);
        }

        return newString(builder.toString());
    }

    private static PythonObject startsWith(PythonString self, PythonObject prefix) {
        return newBoolean(self.string.startsWith(prefix.toString()));
    }

    private static PythonObject format(PythonString self, PythonObject args) {
        StringBuilder builder = new StringBuilder(self.string);
        int start = 0, startCount = 0;
        for (int i = 0; i < builder.length(); i++) {
            char c = builder.charAt(i);
            if (c == '{') {
                start = i;
                startCount++;
            }

            if (c == '}') {
                startCount--;
                if (startCount < 0) {
                    Builtins.ValueError.call(newString("Single '}' encountered in format string"))
                            .raise();
                } else if (startCount > 0) {
                    continue;
                }
                String key = builder.substring(start + 1, i);
                if (key.contains("{")) {
                    Builtins.ValueError.call(newString("unexpected '{' in field name"))
                            .raise();
                }

                PythonObject pKey = newInt(Integer.parseInt(key));
                String value = args.getItem(pKey).toString();
                builder.delete(start, i + 1);
                builder.insert(start, value);

                i = start + value.length() - 1;
            }
        }

        if (startCount > 0) {
            Builtins.ValueError.call(newString("Single '{' encountered in format string"))
                    .raise();
        }

        return newString(builder.toString());
    }

    private static PythonObject getItem(PythonString self, PythonObject key) {
        if (key instanceof PythonInt) {
            int index = getRawIndex(self, key);
            return newString(self.string.substring(index, index + 1));
        }
        PythonObject start = key.getAttribute("start");
        PythonObject end = key.getAttribute("stop");

        if (start == None) {
            start = newInt(0);
        }

        if (end == None) {
            end = newInt(self.string.length());
        }

        if (end.jLessThan(0)) {
            end = newInt(self.string.length()).add(end);
        }

        return newString(self.string.substring(start.toInt(), end.toInt()));
    }

    private static int getRawIndex(PythonString self, PythonObject index) {
        int indexInt = index.toInt();
        int size = self.string.length();

        if (indexInt < 0) {
            indexInt += size;
        }

        return indexInt;
    }
    private static PythonObject len(PythonString self) {
        return newInt(self.string.length());
    }

    private static PythonObject
    replace(PythonString self, PythonObject old, PythonObject new0) {
        return newString(self.string.replace(
                old.toString(), new0.toString()));
    }

    private static PythonObject
    join(PythonString self, PythonObject iterable) {
        StringBuilder builder = new StringBuilder();
        for (PythonObject str : iterable) {
            builder.append(str)
                    .append(self.string);
        }

        int length = builder.length();
        if (length != 0) {
            builder.delete(length - self.string.length(), length);
        }

        return newString(builder.toString());
    }

    private static PythonObject
    strip(PythonObject self) {
        return newString(((PythonString)self).string.strip());
    }

    @Override
    public String toString() {
        return string;
    }
}
