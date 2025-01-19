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
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerPackageLoader;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Re {

    private static PythonObject re;
    private static boolean init;

    public static void initLoader() {
        if (init) {
            return;
        }

        init = true;

        registerPackageLoader("re", (re) -> {
            Re.re = re;

            re.setAttribute("DOTALL", newString("re.DOTALL"));
            re.addNewFunction("compile", Re.class, "compile", new PythonSignatureBuilder("pattern")
                    .addParameter("flags", newInt(0))
                    .build());
            re.addNewFunction("sub", Re.class, "sub", "pattern", "repl", "string");

            PatternImpl.define(re);
            MatchImpl.define(re);
        });
    }

    private static PythonObject
    compile(PythonObject pattern, PythonObject flags) {
        return re.callAttribute("Pattern", pattern, flags);
    }

    private static PythonObject
    sub(PythonObject pattern, PythonObject repl, PythonObject string) {
        return newString(string.toString().replace(pattern.toString(), repl.toString()));
    }

}
