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
 * Copyright (c) 2014-2024 CensoredUsername, Jackmcbarn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software'), to deal
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
 ************************************************************************************/
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.PythonObject.*;
import static com.abiddarris.common.renpy.internal.core.Attributes.callNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Functions.isInstance;
import static com.abiddarris.common.renpy.internal.core.Functions.len;
import static com.abiddarris.common.renpy.internal.core.Functions.not;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.core.classes.BuiltinsClasses.set;
import static com.abiddarris.common.renpy.internal.core.classes.JFunctions.hasattr;
import static com.abiddarris.common.renpy.internal.gen.Generators.newGenerator;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Util {

    private static PythonObject util;

    static void initLoader() {
        registerLoader("decompiler.util", (name) -> {
            util = createModule(name);
            util.fromImport("decompiler.unrpyccompat", "DecompilerBaseAdvanceToLineGenerator");
            util.fromImport("decompiler.unrpyccompat", "DispatcherCallClosure");

            PythonObject OptionBase = OptionBaseImpl.define(util);
            
            DecompilerBaseImpl.define(util, OptionBase);

            util.addNewFunction("reconstruct_paraminfo", Util.class, "reconstructParaminfo", "paraminfo");

            DispatcherImpl.define();
                
            return util;
        });
    }
    
    private static class OptionBaseImpl {
        
        private static PythonObject define(PythonObject util) {
            ClassDefiner definer = util.defineClass("OptionBase");
            definer.defineFunction("__init__", OptionBaseImpl.class, "init",
                 new PythonSignatureBuilder("self")
                    .addParameter("indentation", newString("    "))
                    .addParameter("log", None)
                    .build());
            
            return definer.define();
        }
        
        private static void init(PythonObject self, PythonObject indentation, PythonObject log) {
            self.setAttribute("indentation", indentation);
            self.setAttribute("log", log == None ? newList() : log);
        }
    }
    
    private static class DecompilerBaseImpl {

        private static PythonObject DecompilerBase;
        
        private static PythonObject define(PythonObject util, PythonObject OptionBase) {
            ClassDefiner definer = util.defineClass("DecompilerBase");
            definer.defineFunction("__init__", DecompilerBaseImpl.class, "init", new PythonSignatureBuilder("self")
                .addParameter("out_file", None)
                .addParameter("options", OptionBase.call())
                .build());

            definer.defineFunction("dump", DecompilerBaseImpl.class, "dump", new PythonSignatureBuilder("self", "ast")
                .addParameter("indent_level", newInt(0))
                .addParameter("linenumber", newInt(1))
                .addParameter("skip_indent_until_write", False)
                .build());

            definer.defineFunction("increase_indent", DecompilerBaseImpl.class, "increaseIndent", new PythonSignatureBuilder("self")
                    .addParameter("amount", newInt(1))
                    .build());

            definer.defineFunction("write", DecompilerBaseImpl.class, "write", "self", "string");
            definer.defineFunction("advance_to_line", DecompilerBaseImpl.class, "advanceToLine", "self", "linenumber");

            IndentationContextManagerImpl.define(definer);

            definer.defineFunction("indent", DecompilerBaseImpl.class, "indent", "self");

            definer.defineFunction("print_nodes", DecompilerBaseImpl.class, "printNodes", new PythonSignatureBuilder("self", "ast")
                    .addParameter("extra_indent", newInt(0))
                    .build());

            definer.defineFunction("block", property, DecompilerBaseImpl.class, "block", "self");
            definer.defineFunction("index", property, DecompilerBaseImpl.class, "index", "self");
            definer.defineFunction("parent", property, DecompilerBaseImpl.class, "parent", "self");
            definer.defineFunction("print_debug", DecompilerBaseImpl.class, "printDebug", "self", "message");
            definer.defineFunction("write_failure", DecompilerBaseImpl.class, "writeFailure", "self", "message");
            definer.defineFunction("print_unknown", DecompilerBaseImpl.class, "printUnknown", "self", "ast");
            definer.defineFunction("print_node", DecompilerBaseImpl.class, "printNode", "self", "ast");
            
            return DecompilerBase = definer.define();
        }
        
        private static void init(PythonObject self, PythonObject out_file, PythonObject options) {
            // the file object that the decompiler outputs to
            // FIXME: sys.stdout not supported
            self.setAttribute("out_file", out_file); //or sys.stdout
            // Decompilation options
            self.setAttribute("options", options);
            // the string we use for indentation
            self.setAttribute("indentation", options.getAttribute("indentation"));


            // properties used for keeping track of where we are
            // the current line we're writing.
            self.setAttribute("linenumber", newInt(0));
            // the indentation level we're at
            self.setAttribute("indent_level", newInt(0));
            // a boolean that can be set to make the next call to indent() not insert a newline and
            // indent useful when a child node can continue on the same line as the parent node
            // advance_to_line will also cancel this if it changes the lineno
            self.setAttribute("skip_indent_until_write", False);

            // properties used for keeping track what level of block we're in
            self.setAttribute("block_stack", newList());
            self.setAttribute("index_stack", newList());

            // storage for any stuff that can be emitted whenever we have a blank line
            self.setAttribute("blank_line_queue", newList());
        }
        
        /**
         * Write the decompiled representation of `ast` into the opened file given in the constructor
         */
        private static PythonObject dump(PythonObject self, PythonObject ast, PythonObject indent_level, PythonObject linenumber, PythonObject skip_indent_until_write) {
            self.setAttribute("indent_level", indent_level);
            self.setAttribute("linenumber", linenumber);
            self.setAttribute("skip_indent_until_write", skip_indent_until_write);
            
            if (!isInstance(ast, newTuple(tuple, list)).toBoolean()) {
                ast = newList(ast);
            }
            
            self.callAttribute("print_nodes", ast);
            
            return self.getAttribute("linenumber");
        }

        private static PythonObject increaseIndent(PythonObject self, PythonObject amount) {
            return DecompilerBase.callAttribute("IndentationContextManager", self, amount);
        }

        /**
         * Shorthand method for writing `string` to the file
         */
        private static void write(PythonObject self, PythonObject string) {
            string = str.call(string);

            self.setAttribute("linenumber", self.getAttribute("linenumber").add(
                    string.callAttribute("count", newString("\n"))
            ));
            self.setAttribute("skip_indent_until_write", False);

            callNestedAttribute(self, "out_file.write", string);
        }

        private static void advanceToLine(PythonObject self, PythonObject linenumber) {
            // If there was anything that we wanted to do as soon as we found a blank line,
            // try to do it now.
            self.setAttribute("blank_line_queue", util.callAttribute("DecompilerBaseAdvanceToLineGenerator",
                    self.getAttribute("blank_line_queue"), linenumber));

            if (self.getAttribute("linenumber").jLessThan(linenumber)) {
                // Stop one line short, since the call to indent() will advance the last line.
                // Note that if self.linenumber == linenumber - 1, this will write the empty string.
                // This is to make sure that skip_indent_until_write is cleared in that case.
                self.callAttribute("write", newString("\n").multiply(linenumber.subtract(self.getAttribute("linenumber")).subtract(newInt(1))));
            }
        }

        /**
         * Shorthand method for pushing a newline and indenting to the proper indent level
         * Setting skip_indent_until_write causes calls to this method to be ignored until something
         * calls the write method
         */
        private static void indent(PythonObject self) {
            if (!self.getAttribute("skip_indent_until_write").toBoolean()) {
                self.callAttribute("write", newString("\n").add(
                        self.getAttribute("indentation").multiply(self.getAttribute("indent_level"))
                ));
            }
        }

        private static void printNodes(PythonObject self, PythonObject ast, PythonObject extra_indent) {
            // This node is a list of nodes
            // Print every node
            with(self.callAttribute("increase_indent", extra_indent), () -> {
                self.getAttribute("block_stack").callAttribute("append", ast);
                self.getAttribute("index_stack").callAttribute("append", newInt(0));

                for (PythonObject tuple : enumerate.call(ast)) {
                    PythonObject i = tuple.getItem(newInt(0));
                    PythonObject node = tuple.getItem(newInt(1));

                    self.getAttribute("index_stack").setItem(newInt(-1), i);
                    self.callAttribute("print_node", node);
                }

                self.getAttribute("block_stack").callAttribute("pop");
                self.getAttribute("index_stack").callAttribute("pop");
            });
        }

        private static PythonObject block(PythonObject self) {
            return self.getAttribute("block_stack")
                    .getItem(newInt(-1));
        }


        private static PythonObject index(PythonObject self) {
            return self.getAttribute("index_stack")
                    .getItem(newInt(-1));
        }


        private static PythonObject parent(PythonObject self) {
            if (len(self.getAttribute("block_stack")).jLessThan(newInt( 2))) {
                return None;
            }
            return self.getAttribute("block_stack")
                    .getItem(newInt(-2))
                    .getItem(self.getAttribute("index_stack")
                            .getItem(newInt(-2)));
        }

        private static void printDebug(PythonObject self, PythonObject message) {
            callNestedAttribute(self, "options.log.append", message);
        }

        private static void printUnknown(PythonObject self, PythonObject ast) {
            // If we encounter a placeholder note, print a warning and insert a placeholder
            self.callAttribute("write_failure", newString("Unknown AST node: {0}")
                    .callAttribute("format", type(ast)));
        }

        private static void writeFailure(PythonObject self, PythonObject message) {
            self.callAttribute("print_debug", message);
            self.callAttribute("indent");
            self.callAttribute("write", newString("pass # <<<COULD NOT DECOMPILE: {0}>>>").callAttribute("format", message));
        }

        private static void printNode(PythonObject self, PythonObject ast) {
            NotImplementedError.call().raise();
        }

        private static class IndentationContextManagerImpl {

            private static PythonObject define(ClassDefiner decompilerBaseDefiner) {
                ClassDefiner definer = decompilerBaseDefiner.defineClass("IndentationContextManager");
                definer.defineFunction("__init__", IndentationContextManagerImpl.class, "init", "self", "decompiler_base", "amount");
                definer.defineFunction("__enter__", IndentationContextManagerImpl.class, "enter", "self");
                definer.defineFunction("__exit__", IndentationContextManagerImpl.class, "exit", "self", "p1", "p2", "p3");

                return definer.define();
            }

            private static void init(PythonObject self, PythonObject decompiler_base, PythonObject amount) {
                self.setAttribute("decompiler_base", decompiler_base);
                self.setAttribute("amount", amount);
            }

            private static void enter(PythonObject self) {
                PythonObject decompiler_base = self.getAttribute("decompiler_base");
                PythonObject amount = self.getAttribute("amount");

                PythonObject indent_level = decompiler_base.getAttribute("indent_level");

                decompiler_base.setAttribute("indent_level", newInt(indent_level.toInt() + amount.toInt()));
            }

            private static void exit(PythonObject self, PythonObject p1, PythonObject p2, PythonObject p3) {
                PythonObject decompiler_base = self.getAttribute("decompiler_base");
                PythonObject amount = self.getAttribute("amount");

                PythonObject indent_level = decompiler_base.getAttribute("indent_level");

                decompiler_base.setAttribute("indent_level", newInt(indent_level.toInt() - amount.toInt()));
            }
        }

    }

    private static PythonObject
    reconstructParaminfo(PythonObject paraminfo) {
        if (paraminfo == None) {
            return newString("");
        }

        PythonObject rv = newList(newString("("));
        PythonObject sep = util.callAttribute("First", newString(""), newString(", "));

        if (hasattr(paraminfo, "positional_only")) {
            // ren'py 7.5-7.6 and 8.0-8.1, a slightly changed variant of 7.4 and before

            PythonObject already_accounted = set(
                    newGenerator().forEach(vars -> paraminfo.getAttribute("positional_only"))
                        .name((vars, obj) -> {
                            vars.put("name", obj.getItem(newInt(0)));
                            vars.put("default", obj.getItem(newInt(1)));
                        }).yield(vars -> vars.get("name")));
            already_accounted.callAttribute("update",
                    newGenerator().forEach((vars) -> paraminfo.getAttribute("keyword_only"))
                            .name((vars, obj) -> {
                                vars.put("name", obj.getItem(newInt(0)));
                                vars.put("default", obj.getItem(newInt(1)));
                            }).yield(vars -> vars.get("name")));

            PythonObject other = list.call(
                    newGenerator()
                        .forEach(variables -> paraminfo.getAttribute("parameters"))
                            .name((vars, obj) -> {
                                vars.put("name", obj.getItem(newInt(0)));
                                vars.put("default", obj.getItem(newInt(1)));
                            })
                            .filter(vars -> already_accounted.in(vars.get("name")))
                            .yield(vars -> newTuple(
                                    vars.get("name"), vars.get("default")
                            )));

            for (PythonObject element : paraminfo.getAttribute("positional_only")) {
                PythonObject name = element.getItem(newInt(0));
                PythonObject default0 = element.getItem(newInt(1));

                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", name);
                if (default0 != None) {
                    rv.callAttribute("append", newString("="));
                    rv.callAttribute("append", default0);
                }
            }

            if (paraminfo.getAttribute("positional_only").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("/"));
            }

            for (PythonObject element : other) {
                PythonObject name = element.getItem(newInt(0));
                PythonObject default0 = element.getItem(newInt(1));

                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", name);
                if (default0 != None) {
                    rv.callAttribute("append", newString("="));
                    rv.callAttribute("append", default0);
                }
            }

            if (paraminfo.getAttribute("extrapos").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("*"));
                rv.callAttribute("append", paraminfo.getAttribute("extrapos"));
            } else if (paraminfo.getAttribute("keyword_only").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("*"));
            }

            for (PythonObject element : paraminfo.getAttribute("keyword_only")) {
                PythonObject name = element.getItem(newInt(0));
                PythonObject default0 = element.getItem(newInt(1));

                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", name);
                if (default0 != None) {
                    rv.callAttribute("append", newString("="));
                    rv.callAttribute("append", default0);
                }
            }

            if (paraminfo.getAttribute("extrakw").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("**"));
                rv.callAttribute("append", paraminfo.getAttribute("extrakw"));
            }
        } else if (hasattr(paraminfo, "extrapos")) {
            // ren'py 7.4 and below, python 2 style
            PythonObject positional = list.call(newGenerator()
                    .forEach(vars -> paraminfo.getAttribute("parameters"))
                    .name((vars, obj) -> vars.put("i", obj))
                    .filter(vars -> paraminfo.getAttribute("positional")
                            .in(vars.get("i").getItem(newInt(0))))
                    .yield(vars -> vars.get("i"))
            );
            PythonObject nameonly = list.call(newGenerator()
                    .forEach(vars -> paraminfo.getAttribute("parameters"))
                    .name((vars, obj) -> vars.put("i", obj))
                    .filter(vars -> not(positional.in(vars.get("i"))))
                    .yield(vars -> vars.get("i"))
            );

            for (PythonObject parameter : positional) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", parameter.getItem(newInt(0)));

                if (parameter.getItem(newInt(1)) != None) {
                    rv.callAttribute("append", newString("={0}")
                            .callAttribute("format", parameter.getItem(newInt(1))));
                }
            }

            if (paraminfo.getAttribute("extrapos").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("*{0}")
                        .callAttribute("format", paraminfo.getAttribute("extrapos")));
            }

            if (nameonly.toBoolean()) {
                if (!paraminfo.getAttribute("extrapos").toBoolean()) {
                    rv.callAttribute("append", sep.call());
                    rv.callAttribute("append", newString("*"));
                }

                for (PythonObject parameter : nameonly) {
                    rv.callAttribute("append", sep.call());
                    rv.callAttribute("append", parameter.getItem(newInt(0)));

                    if (parameter.getItem(newInt(1)) != None) {
                        rv.callAttribute("append", newString("={0}")
                                .callAttribute("format", parameter.getItem(newInt(1))));
                    }
                }

            }
            if (paraminfo.getAttribute("extrakw").toBoolean()) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", newString("**{0}")
                        .callAttribute("format", paraminfo.getAttribute("extrakw")));
            }
        } else {
            // ren'py 7.7/8.2 and above.
            // positional only, /, positional or keyword, *, keyword only, ***
            // prescence of the / is indicated by positional only arguments being present
            // prescence of the * (if no *args) are present is indicated by keyword only args
            // being present.
            PythonObject state = newInt(1); // (0 = positional only, 1 = pos/key, 2 = keyword only)

            for (PythonObject parameter : callNestedAttribute(paraminfo, "parameters.values")) {
                rv.callAttribute("append", sep.call());
                if (parameter.getAttribute("kind").equals(newInt(0))) {
                    // positional only
                    state = newInt(0);
                    rv.callAttribute("append", sep.call());
                    rv.callAttribute("append", parameter.getAttribute("name"));

                    if (parameter.getAttribute("default") != None) {
                        rv.callAttribute("append", newString("={0}")
                                .callAttribute("format", parameter.getAttribute("default")));
                    }
                } else {
                    if (state.equals(newInt(0))) {
                        // insert the / if we had a positional only argument before.
                        state = newInt(1);

                        rv.callAttribute("append", newString("/"));
                        rv.callAttribute("append", sep.call());
                    }

                    if (parameter.getAttribute("kind").equals(newInt(1))) {
                        // positional or keyword?
                        rv.callAttribute("append", parameter.getAttribute("name"));
                        if (parameter.getAttribute("default") != None) {
                            rv.callAttribute("append", newString("={0}")
                                    .callAttribute("format", parameter.getAttribute("default")));
                        }
                    } else if (parameter.getAttribute("kind").equals(newInt(2))) {
                        // *positional
                        state = newInt(2);

                        rv.callAttribute("append", newString("*{0}")
                                .callAttribute("format", parameter.getAttribute("name")));
                    } else if (parameter.getAttribute("kind").equals(newInt(3))) {
                        // keyword only
                        if (state.equals(newInt(1))) {
                            // insert the * if we didn't have a *args before
                            state = newInt(2);

                            rv.callAttribute("append", newString("*"));
                            rv.callAttribute("append", sep.call());
                        }

                        rv.callAttribute("append", parameter.getAttribute("name"));

                        if (parameter.getAttribute("default") != None) {
                            rv.callAttribute("append", newString("={0}")
                                    .callAttribute("format", parameter.getAttribute("default")));
                        }
                    } else if (parameter.getAttribute("kind").equals(newInt(4))) {
                        // **keyword
                        state = newInt(3);
                        rv.callAttribute("append", newString("**{0}")
                                .callAttribute("format", parameter.getAttribute("name")));
                    }
                }
            }
        }
        rv.callAttribute("append", newString(")"));

        return newString("").callAttribute("join", rv);
    }

    // Dict subclass for aesthetic dispatching. use @Dispatcher(data) to dispatch
    private static class DispatcherImpl {

        private static PythonObject define() {
            ClassDefiner definer = util.defineClass("Dispatcher", dict);
            definer.defineFunction("__call__", DispatcherImpl.class, "call", "self", "name");

            return definer.define();
        }

        private static PythonObject call(PythonObject self, PythonObject name) {
            return util.getAttribute("DispatcherCallClosure").call(self, name);
        }

    }

}
