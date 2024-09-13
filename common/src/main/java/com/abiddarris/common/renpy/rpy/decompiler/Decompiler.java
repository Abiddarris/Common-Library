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
 ***********************************************************************************/
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.PythonObject.*;
import static com.abiddarris.common.renpy.internal.core.Attributes.callNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Attributes.getNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Functions.isInstance;
import static com.abiddarris.common.renpy.internal.core.Types.type;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Decompiler {
    
    private static PythonObject decompiler;
    
    public static void initLoader() {
        JavaModuleLoader.registerPackageLoader("decompiler", (decompiler) -> {
            Decompiler.decompiler = decompiler;
                
            PythonObject[] imported = decompiler.fromImport("decompiler.util", "DecompilerBase", "OptionBase");
            PythonObject DecompilerBase = imported[0];
            PythonObject OptionBase = imported[1];

            decompiler.fromImport("decompiler.renpycompat", "renpy");
                
            PythonObject Options = OptionsImpl.define(decompiler, OptionBase);
                
            decompiler.addNewFunction("pprint", Decompiler.class, "pprint", new PythonSignatureBuilder("out_file", "ast")
                        .addParameter("options", Options.call())
                        .build());  
                
            PythonObject Decompiler0 = DecompilerImpl.define(decompiler, DecompilerBase);
            System.out.println(Decompiler0.getAttribute("__mro__"));
        });
        Magic.initLoader();
        RenPyCompat.initLoader();
        Util.initLoader();
    }
    
    // Object that carries configurable decompilation options
    private static class OptionsImpl {
        
        private static PythonObject decompiler;
        
        private static PythonObject define(PythonObject decompiler, PythonObject OptionBase) {
            OptionsImpl.decompiler = decompiler;
            
            ClassDefiner definer = decompiler.defineClass("Options", OptionBase);
            definer.defineFunction("__init__", OptionsImpl.class, "init", new PythonSignatureBuilder("self")
                .addParameter("indentation", newString("    "))
                .addParameter("log", None)
                .addParameter("translator", None)
                .addParameter("init_offset", False)
                .addParameter("sl_custom_names", None)
                .build());
            
            return definer.define();
        }
        
        private static void init(PythonObject self, PythonObject indentation, PythonObject log, 
                PythonObject translator, PythonObject init_offset,
                PythonObject sl_custom_names) {
            super0.call(decompiler.getAttribute("Options"), self).callAttribute("__init__", new PythonArgument()
                    .addKeywordArgument("indentation", indentation)
                    .addKeywordArgument("log", log));
            
            // decompilation options
            self.setAttribute("translator", translator);
            self.setAttribute("init_offset", init_offset);
            self.setAttribute("sl_custom_names", sl_custom_names);
        }
    
    }
    
    private static void pprint(PythonObject out_file, PythonObject ast, PythonObject options) {
        decompiler.getAttribute("Decompiler").call(out_file, options).callAttribute("dump", ast);
    }
    
    private static class DecompilerImpl {
        
        private static PythonObject define(PythonObject decompiler, PythonObject DecompilerBase) {
            ClassDefiner definer = decompiler.defineClass("Decompiler", DecompilerBase);
            definer.defineFunction("__init__, ", DecompilerImpl.class, "init", "self", "out_file", "options");
            definer.defineFunction("dump", DecompilerImpl.class, "dump", "self", "ast");
            definer.defineFunction("print_node", DecompilerImpl.class, "printNode", "self", "ast");

            return definer.define();
        }
        
        private static void init(PythonObject self, PythonObject out_file, PythonObject options) {
            super0.call(decompiler.getAttribute("Decompiler"), self).callAttribute("__init__", out_file, options);
            
            self.setAttribute("paired_with", False);
            self.setAttribute("say_inside_menu", None);
            self.setAttribute("label_inside_menu", None);
            self.setAttribute("in_init", False);
            self.setAttribute("missing_init", False);
            self.setAttribute("init_offset", newInt(0));
            self.setAttribute("most_lines_behind", newInt(0));
            self.setAttribute("last_lines_behind", newInt(0));
        }

        private static void dump(PythonObject self, PythonObject ast) {
            if (getNestedAttribute(self, "options.translator").toBoolean()) {
                callNestedAttribute(self, "options.translator.translate_dialogue", ast);
            }
            if (getNestedAttribute(self,"options.init_offset").toBoolean() && isInstance(ast, newTuple(tuple, list)).toBoolean()) {
                self.callAttribute("set_best_init_offset",ast);
            }
            // skip_indent_until_write avoids an initial blank line

           super0.call(decompiler.getAttribute("Decompiler"), self).callAttribute("dump", new PythonArgument(ast)
                   .addKeywordArgument("skip_indent_until_write", True));

            // if there's anything we wanted to write out but didn't yet, do it now

            for (PythonObject m : self.getAttribute("blank_line_queue")) {
                m.call(None);
            }
            self.callAttribute("write", newString("\n# Decompiled by unrpyc: https://github.com/CensoredUsername/unrpyc\n"));
            //assert not self.missing_init, "A required init, init label, or translate block was missing"
        }

        private static void printNode(PythonObject self, PythonObject ast) {
            // We special-case line advancement for some types in their print
            // methods, so don't advance lines for them here.
            if (hasattr.call(ast, newString("linenumber")).toBoolean() && !isInstance(
                    ast, newTuple(
                            getNestedAttribute(decompiler, "renpy.ast.TranslateString"),
                            getNestedAttribute(decompiler, "renpy.ast.With"),
                            getNestedAttribute(decompiler, "renpy.ast.Label"),
                            getNestedAttribute(decompiler, "renpy.ast.Pass"),
                            getNestedAttribute(decompiler, "renpy.ast.Return")
                        )
                    ).toBoolean()) {

                self.callAttribute("advance_to_line", ast.getAttribute("linenumber"));
            }

            callNestedAttribute(self,"dispatch.get", type(ast), type(self).getAttribute("print_unknown"))
                    .call(self, ast);
        }
    }
}
