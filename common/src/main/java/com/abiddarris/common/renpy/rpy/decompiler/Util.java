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
import static com.abiddarris.common.renpy.internal.core.Attributes.getNestedAttribute;
import static com.abiddarris.common.renpy.internal.core.Functions.isInstance;
import static com.abiddarris.common.renpy.internal.core.Functions.len;
import static com.abiddarris.common.renpy.internal.core.Functions.not;
import static com.abiddarris.common.renpy.internal.core.Slice.newSlice;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.core.classes.BuiltinsClasses.set;
import static com.abiddarris.common.renpy.internal.core.JFunctions.hasattr;
import static com.abiddarris.common.renpy.internal.gen.Generators.newGenerator;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;

import com.abiddarris.common.renpy.internal.Builtins;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Util {

    private static PythonObject util;

    static void initLoader() {
        registerLoader("decompiler.util", (name) -> {
            util = createModule(name);
            util.importModule("re");

            util.fromImport("io", "StringIO");
            util.fromImport("decompiler.unrpyccompat", "DecompilerBaseAdvanceToLineGenerator");
            util.fromImport("decompiler.unrpyccompat", "DispatcherCallClosure");

            PythonObject OptionBase = OptionBaseImpl.define(util);
            
            DecompilerBaseImpl.define(util, OptionBase);
            First.define();

            util.addNewFunction("reconstruct_paraminfo", Util.class, "reconstructParaminfo", "paraminfo");

            util.defineFunction("string_escape", Util::stringEscape, "s");

            util.addNewFunction("split_logical_lines", Util.class, "splitLogicalLines", "s");

            // special lexer for simple_expressions the ren'py way
            // false negatives aren't dangerous. but false positives are
            LexerImpl.define();

            DispatcherImpl.define();

            // ren'py string handling
            util.addNewFunction("encode_say_string", Util.class, "encodeSayString", "s");
            util.addNewFunction("say_get_code", Util.class, "sayGetCode", new PythonSignatureBuilder("ast")
                    .addParameter("inmenu", Builtins.False)
                    .build());
                
            return util;
        });
    }
    
    private static class OptionBaseImpl {
        
        private static PythonObject define(PythonObject util) {
            ClassDefiner definer = util.defineClass("OptionBase");
            definer.defineFunction("__init__", OptionBaseImpl.class, "init",
                 new PythonSignatureBuilder("self")
                    .addParameter("indentation", newString("    "))
                    .addParameter("log", Builtins.None)
                    .build());
            
            return definer.define();
        }
        
        private static void init(PythonObject self, PythonObject indentation, PythonObject log) {
            self.setAttribute("indentation", indentation);
            self.setAttribute("log", log == Builtins.None ? newList() : log);
        }
    }
    
    private static class DecompilerBaseImpl {

        private static PythonObject DecompilerBase;
        
        private static PythonObject define(PythonObject util, PythonObject OptionBase) {
            ClassDefiner definer = util.defineClass("DecompilerBase");
            definer.defineFunction("__init__", DecompilerBaseImpl.class, "init", new PythonSignatureBuilder("self")
                .addParameter("out_file", Builtins.None)
                .addParameter("options", OptionBase.call())
                .build());

            definer.defineFunction("dump", DecompilerBaseImpl.class, "dump", new PythonSignatureBuilder("self", "ast")
                .addParameter("indent_level", newInt(0))
                .addParameter("linenumber", newInt(1))
                .addParameter("skip_indent_until_write", Builtins.False)
                .build());

            definer.defineFunction("increase_indent", DecompilerBaseImpl.class, "increaseIndent", new PythonSignatureBuilder("self")
                    .addParameter("amount", newInt(1))
                    .build());

            definer.defineFunction("write", DecompilerBaseImpl.class, "write", "self", "string");
            definer.defineFunction("write_lines", DecompilerBaseImpl.class, "writeLines", "self", "line");
            definer.defineFunction("save_state", DecompilerBaseImpl::saveState, "self");
            definer.defineFunction("commit_state", DecompilerBaseImpl::commitState, "self", "state");
            definer.defineFunction("advance_to_line", DecompilerBaseImpl.class, "advanceToLine", "self", "linenumber");

            IndentationContextManagerImpl.define(definer);

            definer.defineFunction("indent", DecompilerBaseImpl.class, "indent", "self");

            definer.defineFunction("print_nodes", DecompilerBaseImpl.class, "printNodes", new PythonSignatureBuilder("self", "ast")
                    .addParameter("extra_indent", newInt(0))
                    .build());

            definer.defineFunction("block", Builtins.property, DecompilerBaseImpl.class, "block", "self");
            definer.defineFunction("index", Builtins.property, DecompilerBaseImpl.class, "index", "self");
            definer.defineFunction("parent", Builtins.property, DecompilerBaseImpl.class, "parent", "self");
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
            self.setAttribute("skip_indent_until_write", Builtins.False);

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
            
            if (!isInstance(ast, newTuple(Builtins.tuple, Builtins.list)).toBoolean()) {
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
            string = Builtins.str.call(string);

            self.setAttribute("linenumber", self.getAttribute("linenumber").add(
                    string.callAttribute("count", newString("\n"))
            ));
            self.setAttribute("skip_indent_until_write", Builtins.False);

            callNestedAttribute(self, "out_file.write", string);
        }

        /**
         * Write each line in lines to the file without writing whitespace-only lines
         */
        private static void
        writeLines(PythonObject self, PythonObject lines) {
            for (PythonObject line : lines) {
                if (line.equals(newString(""))) {
                    self.callAttribute("write", newString("\n"));
                } else {
                    self.callAttribute("indent");
                    self.callAttribute("write", line);
                }
            }
        }

        /**
         * Save our current state.
         */
        private static PythonObject
        saveState(PythonObject self) {
            PythonObject state = newTuple(self.getAttribute("out_file"),
                     self.getAttribute("skip_indent_until_write"),
                     self.getAttribute("linenumber"),
                     self.getAttribute("block_stack"),
                     self.getAttribute("index_stack"),
                     self.getAttribute("indent_level"),
                     self.getAttribute("blank_line_queue"));
            self.setAttribute("out_file", util.callAttribute("StringIO"));
            return state;
        }

        /**
         * Commit changes since a saved state.
         */
        private static void
        commitState(PythonObject self, PythonObject state) {
            PythonObject out_file = state.getItem(newInt(0));
            out_file.callAttribute("write", callNestedAttribute(self, "out_file.getvalue"));

            self.setAttribute("out_file", out_file);
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

                for (PythonObject tuple : Builtins.enumerate.call(ast)) {
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
                return Builtins.None;
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
            Builtins.NotImplementedError.call().raise();
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

    /**
     * An often used pattern is that on the first item
     * of a loop something special has to be done. This class
     * provides an easy object which on the first access
     * will return True, but any subsequent accesses False
     */
    private static class First {

        private static void define() {
            ClassDefiner definer = util.defineClass("First");
            definer.defineFunction("__init__", First.class, "init", new PythonSignatureBuilder("self")
                    .addParameter("yes_value", Builtins.True)
                    .addParameter("no_value", Builtins.False)
                    .build());
            definer.defineFunction("__call__", First.class, "call", "self");

            definer.define();
        }

        private static void
        init(PythonObject self, PythonObject yes_value, PythonObject no_value) {
            self.setAttribute("yes_value", yes_value);
            self.setAttribute("no_value", no_value);
            self.setAttribute("first", Builtins.True);
        }

        private static PythonObject
        call(PythonObject self) {
            if (self.getAttribute("first").toBoolean()) {
                self.setAttribute("first", Builtins.False);
                return self.getAttribute("yes_value");
            } else {
                return self.getAttribute("no_value");
            }
        }
    }

    private static PythonObject
    reconstructParaminfo(PythonObject paraminfo) {
        if (paraminfo == Builtins.None) {
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

            PythonObject other = Builtins.list.call(
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
                if (default0 != Builtins.None) {
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
                if (default0 != Builtins.None) {
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
                if (default0 != Builtins.None) {
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
            PythonObject positional = Builtins.list.call(newGenerator()
                    .forEach(vars -> paraminfo.getAttribute("parameters"))
                    .name((vars, obj) -> vars.put("i", obj))
                    .filter(vars -> paraminfo.getAttribute("positional")
                            .in(vars.get("i").getItem(newInt(0))))
                    .yield(vars -> vars.get("i"))
            );
            PythonObject nameonly = Builtins.list.call(newGenerator()
                    .forEach(vars -> paraminfo.getAttribute("parameters"))
                    .name((vars, obj) -> vars.put("i", obj))
                    .filter(vars -> not(positional.in(vars.get("i"))))
                    .yield(vars -> vars.get("i"))
            );

            for (PythonObject parameter : positional) {
                rv.callAttribute("append", sep.call());
                rv.callAttribute("append", parameter.getItem(newInt(0)));

                if (parameter.getItem(newInt(1)) != Builtins.None) {
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

                    if (parameter.getItem(newInt(1)) != Builtins.None) {
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

                    if (parameter.getAttribute("default") != Builtins.None) {
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
                        if (parameter.getAttribute("default") != Builtins.None) {
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

                        if (parameter.getAttribute("default") != Builtins.None) {
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

    private static PythonObject
    stringEscape(PythonObject s) {  // TODO see if this needs to work like encode_say_string elsewhere
        s = s.callAttribute("replace", newString("\\"), newString("\\\\"));
        s = s.callAttribute("replace", newString("\""), newString("\\\""));
        s = s.callAttribute("replace", newString("\n"), newString("\\n"));
        s = s.callAttribute("replace", newString("\t"), newString("\\t"));

        return s;
    }

    private static PythonObject
    splitLogicalLines(PythonObject s) {
        return util.callAttribute("Lexer", s)
                .callAttribute("split_logical_lines");
    }

    private static class LexerImpl {

        private static PythonObject define() {
            ClassDefiner definer = util.defineClass("Lexer");
            definer.defineFunction("__init__", LexerImpl.class, "init", "self", "string");
            definer.defineFunction("re", LexerImpl.class, "re", "self", "regexp");
            definer.defineFunction("python_string", LexerImpl.class, "pythonString", new PythonSignatureBuilder("self")
                    .addParameter("clear_whitespace", Builtins.True)
                    .build());

            definer.defineFunction("split_logical_lines", LexerImpl.class, "splitLogicalLines", "self");

            return definer.define();
        }

        private static void
        init(PythonObject self, PythonObject string) {
            self.setAttribute("pos", newInt(0));
            self.setAttribute("length", len(string));
            self.setAttribute("string", string);
        }

        private static PythonObject
        re(PythonObject self, PythonObject regexp) {
            // see if regexp matches at self.string[self.pos].
            // if it does, increment self.pos
            if (self.getAttribute("length").equals(self.getAttribute("pos"))) {
                return Builtins.None;
            }

            PythonObject match = callNestedAttribute(util, "re.compile", regexp,
                    getNestedAttribute(util, "re.DOTALL"))
                    .callAttribute("match", self.getAttribute("string"), self.getAttribute("pos"));
            if (!match.toBoolean()) {
                return Builtins.None;
            }

            self.setAttribute("pos", match.callAttribute("end"));
            return match.callAttribute("group", newInt(0));
        }
        private static PythonObject
        pythonString(PythonObject self, PythonObject clear_whitespace) {
            // parse strings the ren'py way (don't parse docstrings, no b/r in front allowed)
            // edit: now parses docstrings correctly. There was a degenerate case where
            // '''string'string''' would result in issues
            if (clear_whitespace.toBoolean()) {
                return self.callAttribute("match", newString("(u?(\"(?:\"\")?|'(?:'')?).*?(?<=[^\\\\])(?:\\\\\\\\)*(\\2))"));
            } else {
                return self.callAttribute("re", newString("(u?(\"(?:\"\")?|'(?:'')?).*?(?<=[^\\\\])(?:\\\\\\\\)*(\\2))"));
            }
        }

        private static PythonObject
        splitLogicalLines(PythonObject self) {
            // split a sequence in logical lines
            // this behaves similarly to .splitlines() which will ignore
            // a trailing \n
            PythonObject lines = newList();

            PythonObject contained = newInt(0);

            PythonObject startpos = self.getAttribute("pos");

            while (self.getAttribute("pos").jLessThan(self.getAttribute("length"))) {
                PythonObject c = self.getAttribute("string")
                        .getItem(self.getAttribute("pos"));

                if (c.equals(newString("\n"))
                        && !contained.toBoolean()
                        && (!self.getAttribute("pos").toBoolean() || self.getAttribute("string")
                                .getItem(self.getAttribute("pos")
                                        .subtract(newInt(1)))
                                .jNotEquals(newString("\\"))
                            )
                        ) {
                    lines.callAttribute("append", self.getAttribute("string")
                            .getItem(newSlice(startpos, self.getAttribute("pos"))));

                    // the '\n' is not included in the emitted line
                    self.setAttribute("pos", self.getAttribute("pos")
                            .add(newInt(1)));
                    startpos = self.getAttribute("pos");
                    continue;
                }

                if (newTuple(newString("("), newString("["), newString("{")).jin(c)) {
                    contained = contained.add(newInt(1));
                    self.setAttribute("pos", self.getAttribute("pos").add(newInt(1)));
                    continue;
                }

                if (newTuple(newString(")"), newString("]"), newString("}")).jin(c) && contained.toBoolean()) {
                    contained =  contained.subtract(newInt(1));
                    self.setAttribute("pos", self.getAttribute("pos").add(newInt(1)));
                    continue;
                }

                if (c.equals(newString("#"))) {
                    self.callAttribute("re", newString("[^\n]*"));
                    continue;
                }

                if (self.callAttribute("python_string", Builtins.False).toBoolean()) {
                    continue;
                }

                self.callAttribute("re", newString("\\w+| +|."));  // consume a word, whitespace or one symbol
            }

            if (self.getAttribute("pos").equals(startpos)) {
                lines.callAttribute("append", self.getAttribute("string")
                        .getItem(newSlice(startpos)));
            }
            return lines;
        }
    }

    // Dict subclass for aesthetic dispatching. use @Dispatcher(data) to dispatch
    private static class DispatcherImpl {

        private static PythonObject define() {
            ClassDefiner definer = util.defineClass("Dispatcher", Builtins.dict);
            definer.defineFunction("__call__", DispatcherImpl.class, "call", "self", "name");

            return definer.define();
        }

        private static PythonObject call(PythonObject self, PythonObject name) {
            return util.getAttribute("DispatcherCallClosure").call(self, name);
        }

    }

    /**
     * Encodes a string in the format used by Ren'Py say statements.
     */
    private static PythonObject
    encodeSayString(PythonObject s) {
        s = s.callAttribute("replace", newString("\\"), newString("\\\\"));
        s = s.callAttribute("replace", newString("\n"), newString("\\n"));
        s = s.callAttribute("replace", newString("\""), newString("\\\""));
        s = callNestedAttribute(util, "re.sub", newString("(?<= ) "), newString("\\ "), s);

        return newString("\"").add(s)
                .add(newString("\""));
    }

    // Adapted from Ren'Py's Say.get_code
    private static PythonObject
    sayGetCode(PythonObject ast, PythonObject inmenu) {
        PythonObject rv = newList();

        if (ast.getAttribute("who").toBoolean()) {
            rv.callAttribute("append", ast.getAttribute("who"));
        }

        if (hasattr(ast, "attributes") && ast.getAttribute("attributes") != Builtins.None) {
            rv.callAttribute("extend", ast.getAttribute("attributes"));
        }

        if (hasattr(ast, "temporary_attributes") && ast.getAttribute("temporary_attributes") != Builtins.None) {
            rv.callAttribute("append", newString("@"));
            rv.callAttribute("temporary_attributes", ast.getAttribute("temporary_attributes"));
        }

        // no dialogue_filter applies to us

        rv.callAttribute("append", util.callAttribute("encode_say_string",
                ast.getAttribute("what")));

        if (!ast.getAttribute("interact").toBoolean() && !inmenu.toBoolean()) {
            rv.callAttribute("append", newString("nointeract"));
        }

        // explicit_identifier was only added in 7.7/8.2.
        if (hasattr(ast, "explicit_identifier") && ast.getAttribute("explicit_identifier").toBoolean()) {
            rv.callAttribute("append", newString("id"));
            rv.callAttribute("append", ast.getAttribute("identifier"));
        }
        // identifier was added in 7.4.1. But the way ren'py processed it
        // means it doesn't stored it in the pickle unless explicitly set
        else if (hasattr(ast, "identifier") && ast.getAttribute("identifier") != Builtins.None) {
            rv.callAttribute("append", newString("id"));
            rv.callAttribute("append", ast.getAttribute("identifier"));
        }

        if (hasattr(ast, "arguments") && ast.getAttribute("arguments") != Builtins.None) {
            rv.callAttribute("append",
                    util.callAttribute("reconstruct_arginfo", ast.getAttribute("arguments")));
        }

        if (ast.getAttribute("with_").toBoolean()) {
            rv.callAttribute("append", newString("with"));
            rv.callAttribute("append", ast.getAttribute("with_"));
        }

        return newString(" ").callAttribute("join", rv);
    }
}
