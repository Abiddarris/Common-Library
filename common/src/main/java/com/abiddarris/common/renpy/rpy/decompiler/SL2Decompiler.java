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
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.Builtins.super0;
import static com.abiddarris.common.renpy.internal.Python.format;
import static com.abiddarris.common.renpy.internal.core.Functions.bool;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class SL2Decompiler {

    private static PythonObject sl2decompiler;

    static void initLoader() {
        registerLoader("decompiler.sl2decompiler", (sl2decompiler) -> {
            SL2Decompiler.sl2decompiler = sl2decompiler;

            sl2decompiler.fromImport("decompiler.util", "DecompilerBase", "Dispatcher");

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
            ClassDefiner definer = sl2decompiler.defineClass("SL2Decompiler", sl2decompiler.getAttribute("DecompilerBase"));
            definer.defineFunction("__init__", SL2DecompilerImpl::init, "self", "out_file", "options");

            // This dictionary is a mapping of Class: unbound_method, which is used to determine
            // what method to call for which slast class
            PythonObject dispatch = definer.defineAttribute("dispatch", sl2decompiler.callAttribute("Dispatcher"));

            definer.defineFunction("print_node", SL2DecompilerImpl::printNode, "self", "ast");
            definer.defineFunction("print_screen", dispatch.call(sl2decompiler.getNestedAttribute("sl2.slast.SLScreen")),
                    SL2DecompilerImpl::printScreen, "self", "ast");

            definer.define();
        }

        private static void
        init(PythonObject self, PythonObject out_file, PythonObject options) {
            super0.call(sl2decompiler.getAttribute("SL2Decompiler"), self)
                    .callAttribute("__init__", out_file, options);
        }

        private static void
        printNode(PythonObject self, PythonObject ast) {
            self.callAttribute("advance_to_line", ast.getAttribute("location").getItem(1));
            self.callNestedAttribute("dispatch.get", type(ast), type(self).getAttribute("print_unknown"))
                    .call(self, ast);
        }

        private static void
        printScreen(PythonObject self, PythonObject ast) {
            // Print the screen statement and create the block
            self.callAttribute("indent");
            self.callAttribute("write", format("screen {0}", ast.getAttribute("name")));

            // If we have parameters, print them.
            if (ast.getAttributeJB("parameters")) {
                self.callAttribute("write", sl2decompiler.callAttribute(
                        "reconstruct_paraminfo", ast.getAttribute("parameters")));
            }

            // print contents
            PythonObject $args = self.callAttribute("sort_keywords_and_children", ast);
            PythonObject first_line = $args.getItem(0);
            PythonObject other_lines = $args.getItem(1);

            // apparently, screen contents are optional.
            self.callAttribute("print_keyword_or_child", new PythonArgument(first_line)
                    .addKeywordArgument("first_line", True)
                    .addKeywordArgument("has_block", bool(other_lines))
            );

            if (other_lines.toBoolean()) {
                with (self.callAttribute("increase_indent"), () -> {
                    for (PythonObject line : other_lines) {
                        self.callAttribute("print_keyword_or_child", line);
                    }
                });
            }

        }

    }

}
