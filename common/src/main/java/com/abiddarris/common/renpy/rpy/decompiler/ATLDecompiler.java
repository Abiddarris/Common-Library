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
 * Copyright (c) 2012-2024 Yuri K. Schlesner, CensoredUsername, Jackmcbarn
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
 * ***********************************************************************************/
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.core.Functions.isinstance;
import static com.abiddarris.common.renpy.internal.core.JFunctions.hasattr;
import static com.abiddarris.common.renpy.internal.core.JFunctions.jIsinstance;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class ATLDecompiler {

    private static PythonObject atldecompiler;

    static void initLoader() {
        registerLoader("decompiler.atldecompiler", (atldecompiler) -> {
            ATLDecompiler.atldecompiler = atldecompiler;

            atldecompiler.fromImport("decompiler.util", "DecompilerBase", "Dispatcher");
            atldecompiler.importModule("renpy");

            atldecompiler.defineFunction("pprint", ATLDecompiler::pprint,
                    new PythonSignatureBuilder("out_file", "ast", "options")
                            .addParameter("indent_level", 0)
                            .addParameter("linenumber", 1)
                            .addParameter("skip_indent_until_write", False)
                            .build());

            // An object that handles decompilation of atl blocks from the ren'py AST
            ATLDecompilerImpl.define();
        });
    }

    private static PythonObject
    pprint(PythonObject out_file, PythonObject ast, PythonObject options,
           PythonObject indent_level, PythonObject linenumber, PythonObject skip_indent_until_write) {
        return atldecompiler.callAttribute("ATLDecompiler", out_file, options)
                .callAttribute("dump", ast, indent_level, linenumber, skip_indent_until_write);
    }

    private static class ATLDecompilerImpl {

        private static void define() {
            ClassDefiner definer = atldecompiler.defineClass("ATLDecompiler", atldecompiler.getAttribute("DecompilerBase"));
            PythonObject dispatch = definer.defineAttribute("dispatch", atldecompiler.callAttribute("Dispatcher"));

            definer.defineFunction("dump", ATLDecompilerImpl::dump, new PythonSignatureBuilder("self", "ast")
                    .addParameter("indent_level", 0)
                    .addParameter("linenumber", 1)
                    .addParameter("skip_indent_until_write", False)
                    .build());

            definer.defineFunction("print_node", ATLDecompilerImpl::printNode, "self", "ast");
            definer.defineFunction("print_block", ATLDecompilerImpl::printBlock, "self", "block");
            definer.defineFunction("advance_to_block", ATLDecompilerImpl::advanceToBlock, "self", "block");

            definer.define();
        }

        private static PythonObject
        dump(PythonObject self, PythonObject ast, PythonObject indent_level,
             PythonObject linenumber, PythonObject skip_indent_until_write) {
            // At this point, the preceding ":" has been written, and indent hasn't been increased
            // yet. There's no common syntax for starting an ATL node, and the base block that is
            // created is just a RawBlock. normally RawBlocks are created witha block: statement
            // so we cannot just reuse the node for that. Instead, we implement the top level node
            // directly here
            self.setAttribute("indent_level", indent_level);
            self.setAttribute("linenumber", linenumber);
            self.setAttribute("skip_indent_until_write", skip_indent_until_write);

            self.callAttribute("print_block", ast);

            return self.getAttribute("linenumber");
        }

        private static void
        printNode(PythonObject self, PythonObject ast) {
            // Line advancement logic:
            if (hasattr(ast, "loc")) {
                if (jIsinstance(ast, atldecompiler.getNestedAttribute("renpy.atl.RawBlock"))) {
                    self.callAttribute("advance_to_block", ast);
                } else {
                    self.callAttribute("advance_to_line", ast.getAttributeItem("loc", 1));
                }
            }

            self.callNestedAttribute("dispatch.get", type(ast), type(self).getAttribute("print_unknown"))
                    .call(self, ast);
        }

        private static void
        printBlock(PythonObject self, PythonObject block) {
            // Prints a block of ATL statements
            // block is a renpy.atl.RawBlock instance.
            with(self.callAttribute("increase_indent"), () -> {
                if (block.getAttributeJB("statements")) {
                    self.callAttribute("print_nodes", block.getAttribute("statements"));
                }

                // If a statement ends with a colon but has no block after it, loc will
                // get set to ('', 0). That isn't supposed to be valid syntax, but it's
                // the only thing that can generate that, so we do not write "pass" then.
                else if (block.getAttribute("loc").jNotEquals(newTuple(newString(""), newInt(0)))) {
                    // if there were no contents insert a pass node to keep syntax valid.
                    self.callAttribute("indent");
                    self.callAttribute("write", newString("pass"));
                }
            });
        }

        private static void
        advanceToBlock(PythonObject self, PythonObject block) {
            // note: the location property of a RawBlock points to the first line of the block,
            // not the statement that created it.
            // it can also contain the following nonsense if there was no block for some reason.
            if (block.getAttribute("loc").jNotEquals(newTuple(newString(""), newInt(0)))) {
                self.callAttribute("advance_to_line", block.getAttributeItem("loc", 1).subtract(1));
            }
        }

    }
}
