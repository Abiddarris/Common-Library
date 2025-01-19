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

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;

import java.util.regex.Matcher;

class MatchImpl {

    static void define(PythonObject re) {
        ClassDefiner definer = re.defineClass("Match");
        definer.defineFunction("end", MatchImpl.class, "end", "self");
        definer.defineFunction("group", MatchImpl.class, "group", "self", "group");

        definer.define();
    }

    private static PythonObject
    end(PythonObject self) {
        Matcher matcher = self.getJavaAttribute("matcher");

        return newInt(matcher.end());
    }

    private static PythonObject
    group(PythonObject self, PythonObject group) {
        Matcher matcher = self.getJavaAttribute("matcher");

        return newString(matcher.group(group.toInt()));
    }

}
