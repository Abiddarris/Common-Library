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
package com.abiddarris.python3.mod.io;

import static com.abiddarris.python3.Python.newString;

import com.abiddarris.python3.Builtins;
import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.builder.ClassDefiner;

class StringIOImpl {

    private static PythonObject io;

    static PythonObject define(PythonObject io) {
        StringIOImpl.io = io;

        ClassDefiner definer = io.defineClass("StringIO");
        definer.defineFunction("__new__", StringIOImpl.class, "new0", "cls");
        definer.defineFunction("write", StringIOImpl.class, "write", "self", "str");
        definer.defineFunction("getvalue", StringIOImpl.class, "getValue", "self");

        return definer.define();
    }

    private static PythonObject new0(PythonObject cls) {
        PythonObject object = Builtins.object.callAttribute("__new__", cls);
        object.setJavaAttribute("stringBuilder", new StringBuilder());

        return object;
    }

    private static void write(PythonObject self, PythonObject str) {
        StringBuilder builder = self.getJavaAttribute("stringBuilder");
        builder.append(str.toString());
    }

    private static PythonObject getValue(PythonObject self) {
        return newString(self.getJavaAttribute("stringBuilder")
                .toString());
    }

}
