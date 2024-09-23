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

import static com.abiddarris.common.renpy.internal.Python.format;
import static com.abiddarris.common.renpy.internal.PythonObject.False;
import static com.abiddarris.common.renpy.internal.PythonObject.None;
import static com.abiddarris.common.renpy.internal.PythonObject.True;
import static com.abiddarris.common.renpy.internal.PythonObject.enumerate;
import static com.abiddarris.common.renpy.internal.PythonObject.hasattr;
import static com.abiddarris.common.renpy.internal.PythonObject.list;
import static com.abiddarris.common.renpy.internal.PythonObject.newInt;
import static com.abiddarris.common.renpy.internal.PythonObject.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.newTuple;
import static com.abiddarris.common.renpy.internal.PythonObject.super0;
import static com.abiddarris.common.renpy.internal.PythonObject.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.tuple;
import static com.abiddarris.common.renpy.internal.core.Attributes.callNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Attributes.getNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Functions.all;
import static com.abiddarris.common.renpy.internal.core.Functions.isInstance;
import static com.abiddarris.common.renpy.internal.core.Functions.isinstance;
import static com.abiddarris.common.renpy.internal.core.Functions.len;
import static com.abiddarris.common.renpy.internal.core.Slice.newSlice;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.core.classes.JFunctions.hasattr;
import static com.abiddarris.common.renpy.internal.with.With.with;

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
                
            PythonObject[] imported = decompiler.fromImport("decompiler.util",
                    "DecompilerBase", "reconstruct_paraminfo", "split_logical_lines",
                    "Dispatcher", "OptionBase");
            PythonObject DecompilerBase = imported[0];
            PythonObject OptionBase = imported[4];

            decompiler.fromImport("decompiler.renpycompat", "renpy");
            decompiler.fromImport("io", "StringIO");
            decompiler.fromImport("decompiler.unrpyccompat", "DecompilerPrintInit");

            PythonObject Options = OptionsImpl.define(decompiler, OptionBase);
                
            decompiler.addNewFunction("pprint", Decompiler.class, "pprint", new PythonSignatureBuilder("out_file", "ast")
                        .addParameter("options", Options.call())
                        .build());  
                
            PythonObject Decompiler0 = DecompilerImpl.define(decompiler, DecompilerBase);
        });
        UnRpycCompat.initLoader();
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

    /**
     * An object which hanldes the decompilation of renpy asts to a given stream
     */
    private static class DecompilerImpl {

        private static PythonObject define(PythonObject decompiler, PythonObject DecompilerBase) {
            ClassDefiner definer = decompiler.defineClass("Decompiler", DecompilerBase);

            // This dictionary is a mapping of Class: unbount_method, which is used to determine
            // what method to call for which ast class
            PythonObject dispatch = definer.defineAttribute("dispatch", decompiler.getAttribute("Dispatcher").call());

            definer.defineFunction("__init__", DecompilerImpl.class, "init", "self", "out_file", "options");
            definer.defineFunction("dump", DecompilerImpl.class, "dump", "self", "ast");
            definer.defineFunction("print_node", DecompilerImpl.class, "printNode", "self", "ast");

            // Flow control
            definer.defineFunction("print_label", dispatch.call(getNestedAttribute(decompiler, "renpy.ast.Label")), DecompilerImpl.class, "printLabel", "self", "ast");
            definer.defineFunction("print_if", dispatch.call(getNestedAttribute(decompiler, "renpy.ast.If")), DecompilerImpl.class, "printIf", "self", "ast");

            definer.defineFunction("should_come_before", DecompilerImpl.class, "shouldComeBefore", "self", "first", "second");
            definer.defineFunction("require_init", DecompilerImpl.class, "requireInit", "self");
            definer.defineFunction("print_init", dispatch.call(getNestedAttribute(decompiler, "renpy.ast.Init")),
                    DecompilerImpl.class, "printInit", "self", "ast");

            // Programming related functions

            definer.defineFunction("print_python", dispatch.call(getNestedAttribute(decompiler, "renpy.ast.Python")),
                    DecompilerImpl.class, "printPython", new PythonSignatureBuilder("self", "ast")
                            .addParameter("early", False)
                            .build());


            definer.defineFunction("print_define", dispatch.call(getNestedAttribute(decompiler, "renpy.ast.Define")),
                    DecompilerImpl.class, "printDefine", "self", "ast");

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

        private static void printLabel(PythonObject self, PythonObject ast) {
            // If a Call block preceded us, it printed us as "from"
            if (self.getAttribute("index").toBoolean() &&
                    isinstance(self.getAttribute("block")
                                     .getItem(self.getAttribute("index")
                                                 .subtract(newInt(1))
                                     ),
                            getNestedAttribute(decompiler, "renpy.ast.Call")
                    ).toBoolean()) {
                return;
            }

            // See if we're the label for a menu, rather than a standalone label.
            if (!ast.getAttribute("block").toBoolean() &&
                    ast.getAttribute("parameters") == None) {
                PythonObject remaining_blocks = len(self.getAttribute("block"))
                        .subtract(self.getAttribute("index"));

                PythonObject next_ast = None;
                if (remaining_blocks.jGreaterThan(newInt(1))) {
                    // Label followed by a menu
                    next_ast = self.getAttribute("block")
                            .getItem(self.getAttribute("index")
                                    .add(newInt(1)));
                    if (isinstance(next_ast, getNestedAttribute(decompiler, " renpy.ast.Menu")).toBoolean() &&
                            next_ast.getAttribute("linenumber").equals(ast.getAttribute("linenumber"))) {
                        self.setAttribute("label_inside_menu", ast);
                        return;
                    }
                }

                if (remaining_blocks.jGreaterThan(newInt(2))) {
                    // Label, followed by a say, followed by a menu
                    PythonObject next_next_ast = self.getAttribute("block")
                            .getItem(self.getAttribute("index")
                                    .add(newInt(2)));
                    if (isinstance(next_ast, getNestedAttribute(decompiler, "renpy.ast.Say")).toBoolean()
                            && isinstance(next_next_ast, getNestedAttribute(decompiler, "renpy.ast.Menu")).toBoolean()
                            && next_next_ast.getAttribute("linenumber").equals(ast.getAttribute("linenumber"))
                            && self.callAttribute("say_belongs_to_menu", next_ast, next_next_ast).toBoolean()) {
                        self.setAttribute("label_inside_menu", ast);
                        return;
                    }
                }
            }

            self.callAttribute("advance_to_line", ast.getAttribute("linenumber"));
            self.callAttribute("indent");

            // It's possible that we're an "init label", not a regular label. There's no way to know
            // if we are until we parse our children, so temporarily redirect all of our output until
            // that's done, so that we can squeeze in an "init " if we are.
            PythonObject out_file = self.getAttribute("out_file");
            self.setAttribute("out_file", decompiler.callAttribute("StringIO"));

            PythonObject missing_init = self.getAttribute("missing_init");
            self.setAttribute("missing_init", False);

            tryExcept(() -> {
                self.callAttribute("write", newString("label {0}{1}{2}:")
                        .callAttribute("format",
                                ast.getAttribute("name"),
                                decompiler.callAttribute("reconstruct_paraminfo", ast.getAttribute("parameters")),
                                ast.getAttribute("hide", False).toBoolean() ?
                                        newString(" hide") : newString("")
                        )
                );
                self.callAttribute("print_nodes", ast.getAttribute("block"), newInt(1));
            }).onFinally(() -> {
                if (self.getAttribute("missing_init").toBoolean()) {
                    out_file.callAttribute("write", newString("init "));
                }
                self.setAttribute("missing_init", missing_init);
                out_file.callAttribute("write", callNestedAttribute(self, "out_file.getvalue"));
                self.setAttribute("out_file", out_file);
            });
        }

        private static void
        printIf(PythonObject self, PythonObject ast) {
            PythonObject statement = decompiler.callAttribute("First", newString("if"), newString("elif"));

            for (PythonObject element : enumerate.call(ast.getAttribute("entries"))) {
                PythonObject i = element.getItem(newInt(0));
                element = element.getItem(newInt(1));

                PythonObject condition = element.getItem(newInt(0));
                PythonObject block = element.getItem(newInt(1));

                // The unicode string "True" is used as the condition for else:.
                // But if it's an actual expression, it's a renpy.ast.PyExpr
                if (i.add(newInt(1)).equals(len(ast.getAttribute("entries"))) &&
                        !isinstance(condition, getNestedAttribute(decompiler, "renpy.ast.PyExpr")).toBoolean()) {
                    self.callAttribute("indent");
                    self.callAttribute("write", newString("else:"));
                } else {
                    if (hasattr(condition, "linenumber")) {
                        self.callAttribute("advance_to_line", condition.getAttribute("linenumber"));
                    }
                    self.callAttribute("indent");
                    self.callAttribute("write",format("{0} {1}:", statement.call(), condition));
                }

                self.callAttribute("print_nodes", block, newInt(1));
            }
        }

        private static PythonObject shouldComeBefore(PythonObject self, PythonObject first, PythonObject second) {
            return first.getAttribute("linenumber").lessThan(second.getAttribute("linenumber"));
        }

        private static void requireInit(PythonObject self) {
            if (!self.getAttribute("in_init").toBoolean()) {
                self.getAttribute("missing_init", True);
            }
        }

        private static void printInit(PythonObject self, PythonObject ast) {
            PythonObject in_init = self.getAttribute("in_init");
            self.setAttribute("in_init", True);
            tryExcept(() -> {
                // A bunch of statements can have implicit init blocks
                // Define has a default priority of 0, screen of -500 and image of 990
                // Keep this block in sync with set_best_init_offset
                // TODO merge this and require_init into another decorator or something
                if (len(ast.getAttribute("block")).equals(newInt(1))
                        && (isinstance(
                                ast.getAttribute("block").getItem(newInt(0)),
                                newTuple(
                                        getNestedAttribute(decompiler, "renpy.ast.Define"),
                                        getNestedAttribute(decompiler, "renpy.ast.Default"),
                                        getNestedAttribute(decompiler, "renpy.ast.Transform")
                                )
                            ).toBoolean()
                        || (ast.getAttribute("priority").equals(
                                        newInt(-500).add(self.getAttribute("init_offset"))
                                )
                            && isinstance(
                                    ast.getAttribute("block").getItem(newInt(0)),
                                    getNestedAttribute(decompiler, "renpy.ast.Screen")
                               ).toBoolean()
                            )
                        || (ast.getAttribute("priority").equals(self.getAttribute("init_offset"))
                                 && isinstance(
                                         ast.getAttribute("block").getItem(newInt(0)),
                                         getNestedAttribute(decompiler, "renpy.ast.Style")
                                ).toBoolean()
                            )
                        || (ast.getAttribute("priority").equals(
                                newInt(500).add(self.getAttribute("init_offset"))
                            )
                            && isinstance(
                                    ast.getAttribute("block").getItem(newInt(0)),
                                    getNestedAttribute(decompiler, "renpy.ast.Testcase")
                                ).toBoolean()
                            )
                        || (ast.getAttribute("priority").equals(
                                newInt(0).add(self.getAttribute("init_offset"))
                            )
                            && isinstance(
                                    ast.getAttribute("block").getItem(newInt(0)),
                                    getNestedAttribute(decompiler, "renpy.ast.UserStatement")
                                ).toBoolean()
                            && callNestedAttribute(
                                    ast.getAttribute("block").getItem(newInt(0)),
                                    "line.startswith",
                                    newString("layeredimage ")
                                ).toBoolean()
                            )
                        || (ast.getAttribute("priority").equals(
                                newInt(500).add(self.getAttribute("init_offset"))
                            )
                            && isinstance(
                                    ast.getAttribute("block").getItem(newInt(0)),
                                    getNestedAttribute(decompiler, "renpy.ast.Image")
                                ).toBoolean()
                            )
                        )
                        && !(self.callAttribute("should_come_before",
                                ast, ast.getAttribute("block").getItem(newInt(0))
                            ).toBoolean()
                        )) {

                    // If they fulfill this criteria we just print the contained statement
                    self.callAttribute("print_nodes", ast.getAttribute("block"));
                }
                // translatestring statements are split apart and put in an init block.
                else if (len(ast.getAttribute("block")).toInt() > 0
                      && ast.getAttribute("priority").equals(self.getAttribute("init_offset"))
                      && all(decompiler.callAttribute(
                              "DecompilerPrintInit", decompiler.getAttribute("renpy"), ast)
                            ).toBoolean()
                      && all(decompiler.callAttribute("DecompilerPrintInit1", ast))
                            .toBoolean()) {
                    self.callAttribute("print_nodes", ast.getAttribute("block"));
                } else {
                    self.callAttribute("indent");
                    self.callAttribute("write", newString("init"));
                    if (ast.getAttribute("priority").jNotEquals(self.getAttribute("init_offset"))) {
                        self.callAttribute("write", newString("{0}").callAttribute("format",
                                ast.getAttribute("priority").subtract(self.getAttribute("init_offset"))
                        ));
                    }

                    if (len(ast.getAttribute("block")).equals(newInt(1))
                            && !self.callAttribute(
                                    "should_come_before", ast,
                                     ast.getAttribute("block").getItem(newInt(0))
                            ).toBoolean()) {
                        self.callAttribute("write", newString(" "));
                        self.setAttribute("skip_indent_until_write", True);
                        self.callAttribute("print_nodes", ast.getAttribute("block"));
                    } else {
                        self.callAttribute("write", newString(":"));
                        self.callAttribute("print_nodes", ast.getAttribute("block"), newInt(1));
                    }
                }
            }).onFinally(() -> self.setAttribute("in_init", in_init));
        }

        private static void
        printPython(PythonObject self, PythonObject ast, PythonObject early) {
            self.callAttribute("indent");

            PythonObject code = getNestedAttribute(ast, "code.source");
            if (code.getItem(newInt(0)).equals(newString("\n"))) {
                code = code.getItem(newSlice(1));
                self.callAttribute("write", newString("python"));

                if (early.toBoolean()) {
                    self.callAttribute("write", newString(" early"));
                }
                if (ast.getAttribute("hide").toBoolean()) {
                    self.callAttribute("write", newString(" hide"));
                }
                // store attribute added in 6.14
                if (ast.getAttribute("store", newString("store")).jNotEquals(newString("store"))) {
                    self.callAttribute("write", newString(" in "));
                    // Strip prepended "store."
                    self.callAttribute("write", ast.getAttribute("store").getItem(newSlice(6)));
                }
                self.callAttribute("write", newString(":"));

                // Fix annoying lambda
                PythonObject code0 = code;
                with(self.callAttribute("increase_indent"), () -> {
                    self.callAttribute("write_lines", decompiler.callAttribute("split_logical_lines", code0));
                });
            } else {
                self.callAttribute("write", format("$ {0}", code));
            }
        }

        private static void printDefine(PythonObject self, PythonObject ast) {
            self.callAttribute("require_init");
            self.callAttribute("indent");

            // If we have an implicit init block with a non-default priority, we need to store
            // the priority here.
            PythonObject priority = newString("");
            if (isinstance(self.getAttribute("parent"),
                    getNestedAttribute(decompiler, "renpy.ast.Init")
            ).toBoolean()) {
                PythonObject init = self.getAttribute("parent");
                if (init.getAttribute("priority").jNotEquals(self.getAttribute("init_offset"))
                        && len(init.getAttribute("block")).equals(newInt(0))
                        && !self.callAttribute("should_come_before", init, ast).toBoolean()) {
                    priority = newString(" {0}").callAttribute("format",
                            init.getAttribute("priority")
                                    .subtract(self.getAttribute("init_offset")));
                }
            }
            PythonObject index = newString("");
            // index attribute added in 7.4
            if (ast.getAttribute("index", None) != None) {
                index = newString("[{0}]").callAttribute("format", getNestedAttribute(ast, "index.source"));
            }

            // operator attribute added in 7.4
            PythonObject operator = ast.getAttribute("operator", newString("="));

            // store attribute added in 6.18.2
            if (ast.getAttribute("store", newString("store")).equals(newString("store"))) {
                self.callAttribute("write", newString("define{0} {1}{2} {3} {4}")
                        .callAttribute("format", priority, ast.getAttribute("varname"), index,
                                operator, getNestedAttribute(ast, "code.source")));
            } else {
                self.callAttribute("write", newString("define{0} {1}.{2}{3} {4} {5}")
                        .callAttribute("format", priority, ast.getAttribute("store").getItem(newSlice(6)),
                                ast.getAttribute("varname"), index, operator,
                                getNestedAttribute(ast, "code.source")));
            }
        }
    }

}
