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
package com.abiddarris.common.renpy.internal.mod.re;

import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.int0;
import static com.abiddarris.common.renpy.internal.core.Functions.isinstance;
import static java.util.regex.Pattern.DOTALL;
import static java.util.regex.Pattern.compile;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

class PatternImpl {

    public static void define(PythonObject re) {
        ClassDefiner definer = re.defineClass("Pattern");
        definer.defineFunction("__init__", PatternImpl.class, "init", "self", "pattern", "flags");

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


}
