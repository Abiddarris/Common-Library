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
package com.abiddarris.python3.mod.re;

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Builtins.None;
import static com.abiddarris.python3.Builtins.int0;
import static com.abiddarris.python3.core.Functions.isinstance;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;
import com.abiddarris.python3.signature.PythonSignatureBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PatternImpl {

    private static PythonObject re;

    public static void define(PythonObject re) {
        PatternImpl.re = re;

        ClassDefiner definer = re.defineClass("Pattern");
        definer.defineFunction("__init__", PatternImpl.class, "init", "self", "pattern", "flags");
        definer.defineFunction("match", PatternImpl.class, "match", new PythonSignatureBuilder("self", "string")
                .addParameter("start", newInt(0))
                .build());

        definer.define();
    }

    private static void init(PythonObject self, PythonObject pattern, PythonObject flags) {
        int flag = 0;
        if (!isinstance(flags, int0).toBoolean()) {
            if(flags.equals(newString("re.DOTALL"))) {
                flag |= DOTALL;
            }
        }
        self.setJavaAttribute("pattern", compile(pattern.toString(), flag));
    }

    private static PythonObject
    match(PythonObject self, PythonObject string, PythonObject start) {
        Pattern pattern = self.getJavaAttribute("pattern");
        Matcher matcher = pattern.matcher(string.toString());
        matcher.region(start.toInt(), matcher.regionEnd());
        if (!matcher.lookingAt()) {
            return None;
        }

        PythonObject match = re.callAttribute("Match");
        match.setJavaAttribute("matcher", matcher);

        return match;
    }

}
