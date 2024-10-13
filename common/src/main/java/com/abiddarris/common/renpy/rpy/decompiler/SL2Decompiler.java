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
 *
 * Original MIT License :
 *
 * Copyright (c) 2014-2024 CensoredUsername, Jackmcbarn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***********************************************************************************/
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Builtins.super0;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class SL2Decompiler {

    private static PythonObject sl2decompiler;

    static void initLoader() {
        registerLoader("decompiler.sl2decompiler", (sl2decompiler) -> {
            SL2Decompiler.sl2decompiler = sl2decompiler;

            // Main API
            sl2decompiler.defineFunction("pprint", SL2Decompiler::pprint,
                    new PythonSignatureBuilder("out_file", "ast", "options")
                            .addParameter("indent_level", 0)
                            .addParameter("linenumber", 1)
                            .addParameter("skip_indent_until_write", False)
                            .build());

            SL2DecompilerImpl.define();
        });
    }

    private static PythonObject
    pprint(PythonObject out_file, PythonObject ast, PythonObject options,
           PythonObject indent_level, PythonObject linenumber, PythonObject skip_indent_until_write) {
        return sl2decompiler.callAttribute("SL2Decompiler", out_file, options)
                .callAttribute("dump", ast, indent_level, linenumber, skip_indent_until_write);
    }

    private static class SL2DecompilerImpl {

        private static void define() {
            ClassDefiner definer = sl2decompiler.defineClass("SL2Decompiler");
            definer.defineFunction("__init__", SL2DecompilerImpl::init, "self", "out_file", "options");

            definer.define();
        }

        private static void
        init(PythonObject self, PythonObject out_file, PythonObject options) {
            super0.call(sl2decompiler.getAttribute("SL2Decompiler"), self)
                    .callAttribute("__init__", out_file, options);
        }

    }

}
