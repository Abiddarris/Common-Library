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
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.Python.format;
import static com.abiddarris.common.renpy.internal.Python.newBoolean;
import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.core.Functions.isinstance;
import static com.abiddarris.common.renpy.internal.core.Functions.len;
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

            atldecompiler.fromImport("decompiler.util", "DecompilerBase", "WordConcatenator", "Dispatcher");
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

            definer.defineFunction("print_atl_rawmulti", dispatch.call(atldecompiler.getNestedAttribute("renpy.atl.RawMultipurpose")),
                    ATLDecompilerImpl::printAtlRawmulti, "self", "ast");

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
                // get set to ('', 0). That isn't supposed to be valid syntax, but it's// the only thing that can generate that, so we do not write "pass" then.
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

        private static void
        printAtlRawmulti(PythonObject self, PythonObject ast) {
            PythonObject warp_words = atldecompiler.callAttribute("WordConcatenator", False);

            // warpers
            // I think something changed about the handling of pause, that last special case
            // doesn't look necessary anymore as a proper pause warper exists now but we'll
            // keep it around for backwards compatability
            if (ast.getAttributeJB("warp_function")) {
                warp_words.callAttribute("append", newString("warp"), ast.getAttribute("warp_function"), ast.getAttribute("duration"));
            } else if (ast.getAttributeJB("warper")) {
                warp_words.callAttribute("append", ast.getAttribute("warper"), ast.getAttribute("duration"));
            } else if (ast.getAttribute("duration").jNotEquals("0")) {
                warp_words.callAttribute("append", newString("pause"), ast.getAttribute("duration"));
            }

            PythonObject warp = warp_words.callAttribute("join");
            PythonObject words = atldecompiler.callAttribute("WordConcatenator",
                    newBoolean(warp.toBoolean() && warp.getItem(-1).jNotEquals(" ")), True);

            // revolution
            if (ast.getAttributeJB("revolution")) {
                words.callAttribute("append", ast.getAttribute("revolution"));
            }

            // circles
            if (ast.getAttribute("circles").jNotEquals("0")) {
                words.callAttribute("append", format("circles {0}", ast.getAttribute("circles")));
            }

            // splines
            PythonObject spline_words = atldecompiler.callAttribute("WordConcatenator", False);
            for (PythonObject $args : ast.getAttribute("splines")) {
                PythonObject name = $args.getItem(2), expressions = $args.getItem(1);

                spline_words.callAttribute("append", name, expressions.getItem(-1));
                for (PythonObject expression : expressions.sliceTo(-1)) {
                    spline_words.callAttribute("append", newString("knot"), expression);
                }
            }

            words.callAttribute("append", spline_words.callAttribute("join"));

            // properties
            PythonObject property_words = atldecompiler.callAttribute("WordConcatenator", False);
            for (PythonObject $args : ast.getAttribute("properties")) {
                PythonObject key = $args.getItem(0), value = $args.getItem(1);
                property_words.callAttribute("append", key, value);
            }
            words.callAttribute("append", property_words.callAttribute("join"));

            // with
            PythonObject expression_words = atldecompiler.callAttribute("WordConcatenator", False);
            // TODO There's a lot of cases where pass isn't needed, since we could
            // reorder stuff so there's never 2 expressions in a row. (And it's never
            // necessary for the last one, but we don't know what the last one is
            // since it could get reordered.)
            PythonObject needs_pass = len(ast.getAttribute("expressions")).greaterThan(1);
            for (PythonObject $args : ast.getAttribute("expressions")) {
                PythonObject expression = $args.getItem(0), with_expression = $args.getItem(1);
                expression_words.callAttribute("append", expression);
                if (with_expression.toBoolean()) {
                    expression_words.callAttribute("append", newString("with"), with_expression);
                }
                if (needs_pass.toBoolean()) {
                    expression_words.callAttribute("append", newString("pass"));
                }
            }

            words.callAttribute("append", expression_words.callAttribute("join"));

            PythonObject to_write = warp.add(words.callAttribute("join"));
            if (to_write.toBoolean()) {
                self.callAttribute("indent");
                self.callAttribute("write", to_write);
            } else {
                // A trailing comma results in an empty RawMultipurpose being
                // generated on the same line as the last real one.
                self.callAttribute("write", newString(","));
            }
        }
    }
}
