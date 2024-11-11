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
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class ATLDecompiler {

    private static PythonObject atldecompiler;

    static void initLoader() {
        registerLoader("decompiler.atldecompiler", (atldecompiler) -> {
            ATLDecompiler.atldecompiler = atldecompiler;

            atldecompiler.fromImport("decompiler.util", "DecompilerBase");

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
            definer.defineFunction("dump", ATLDecompilerImpl::dump, new PythonSignatureBuilder("self", "ast")
                    .addParameter("indent_level", 0)
                    .addParameter("linenumber", 1)
                    .addParameter("skip_indent_until_write", False)
                    .build());

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

    }
}
