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
import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.Builtins.super0;
import static com.abiddarris.common.renpy.internal.Python.format;
import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newList;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.core.BuiltinsClass.enumerate;
import static com.abiddarris.common.renpy.internal.core.BuiltinsClass.list;
import static com.abiddarris.common.renpy.internal.core.BuiltinsClass.range;
import static com.abiddarris.common.renpy.internal.core.Functions.bool;
import static com.abiddarris.common.renpy.internal.core.Functions.len;
import static com.abiddarris.common.renpy.internal.core.JFunctions.getattr;
import static com.abiddarris.common.renpy.internal.core.Types.type;
import static com.abiddarris.common.renpy.internal.gen.Generators.newGenerator;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;
import static com.abiddarris.common.renpy.internal.with.With.with;

import com.abiddarris.common.renpy.internal.Python;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class SL2Decompiler {

    private static PythonObject sl2decompiler;

    static void initLoader() {
        registerLoader("decompiler.sl2decompiler", (sl2decompiler) -> {
            SL2Decompiler.sl2decompiler = sl2decompiler;

            sl2decompiler.fromImport("decompiler.util", "DecompilerBase", "reconstruct_paraminfo", "Dispatcher");
            sl2decompiler.fromImport("renpy", "sl2");

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

            definer.defineFunction("sort_keywords_and_children", SL2DecompilerImpl::sortKeywordsAndChildren,
                    new PythonSignatureBuilder("self", "node")
                            .addParameter("immediate_block", False)
                            .addParameter("ignore_children", False)
                            .build());
            definer.defineFunction("print_keyword_or_child", SL2DecompilerImpl::printKeywordOrChild,
                    new PythonSignatureBuilder("self", "item")
                            .addParameter("first_line", False)
                            .addParameter("has_block", False)
                            .build());

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

        private static PythonObject
        sortKeywordsAndChildren(PythonObject self, PythonObject node,
                                   PythonObject immediate_block, PythonObject ignore_children) {
            // sorts the contents of a SL statement that has keywords and children
            // returns a list of sorted contents.
            //
            // node is either a SLDisplayable, a SLScreen or a SLBlock
            //
            // before this point, the name and any positional arguments of the statement have been
            // emitted, but the block itself has not been created yet.
            //   immediate_block: bool, if True, nothing is on the first line
            //   ignore_children: Do not inspect children, used to implement "has" statements

            // get all the data we need from the node
            PythonObject keywords = node.getAttribute("keyword");
            PythonObject children = ignore_children.toBoolean() ? newList() : node.getAttribute("children");

            // first linenumber where we can insert content that doesn't have a clear lineno
            PythonObject block_lineno = node.getAttribute("location").getItem(1);
            PythonObject start_lineno = immediate_block.toBoolean() ? block_lineno.add(1) : block_lineno;

            // these ones are optional
            PythonObject keyword_tag = getattr(node, "tag", None);  // only used by SLScreen
            PythonObject keyword_as = getattr(node, "variable", None);  // only used by SLDisplayable

            // all three can have it, but it is an optional property anyway
            PythonObject atl_transform = getattr(node, "atl_transform", None);

            // keywords
            // pre 7.7/8.2: keywords at the end of a line could not have an argument and the parser
            // was okay with that.
            PythonObject keywords_by_line = list(newGenerator()
                    .forEach(() -> keywords, "name", "value")
                    .yield(vars -> {
                        PythonObject name = vars.get("name");
                        PythonObject value = vars.get("value");

                        return newTuple(
                                value.toBoolean() ? value.getAttribute("linenumber") : None,
                                newString(value.toBoolean() ? "keyword" : "broken"),
                                newTuple(name, value)
                        );
                    }));

            // children
            PythonObject children_by_line = list(newGenerator()
                    .forEach(() -> children, "child")
                    .yield(vars -> {
                        PythonObject child = vars.get("child");

                        return newTuple(child.getAttribute("location").getItem(1),
                                newString("child"), child);
                    }));


            // now we have to determine the order of all things. Multiple keywords can go on the
            // same line, but not children. we don't want to completely trust lineno's, even if
            // they're utterly wrong we still should spit out a decent file also, keywords and
            // children are supposed to be in order from the start, so we shouldn't scramble that.

            // merge keywords and childrens into a single ordered list
            // list of lineno, type, contents
            PythonObject contents_in_order = newList();

            keywords_by_line.callAttribute("reverse");
            children_by_line.callAttribute("reverse");

            while (keywords_by_line.toBoolean() && children_by_line.toBoolean()) {
                // broken keywords: always emit before any children, so we can merge them with the
                // previous keywords easily
                if (keywords_by_line.getItem(-1).getItem(0) == None) {
                    contents_in_order.callAttribute("append", keywords_by_line.callAttribute("pop"));
                } else if (keywords_by_line.getItem(-1).getItem(0)
                        .jLessThan(children_by_line.getItem(-1).getItem(0))) {
                    contents_in_order.callAttribute("append", keywords_by_line.callAttribute("pop"));
                } else {
                    contents_in_order.callAttribute("append", children_by_line.callAttribute("pop"));
                }
            }

            while (keywords_by_line.toBoolean()) {
                contents_in_order.callAttribute("append", keywords_by_line.callAttribute("pop"));
            }

            while (children_by_line.toBoolean()) {
                contents_in_order.callAttribute("append", children_by_line.callAttribute("pop"));
            }

            // merge in at transform if present
            if (atl_transform != None) {
                PythonObject atl_lineno = atl_transform.getAttribute("loc").getItem(1);

                boolean $executeElse = true;
                PythonObject index = None;
                for (PythonObject $args : enumerate(contents_in_order)) {
                    PythonObject i = $args.getItem(0);
                    $args = $args.getItem(1);

                    PythonObject lineno = $args.getItem(0);
                    if (lineno != None && atl_lineno.jGreaterThan(lineno)) {
                        index = i;
                        $executeElse = false;
                        break;
                    }
                }
                if ($executeElse) {
                    index = len(contents_in_order);
                }

                contents_in_order.callAttribute("insert", index, newTuple(atl_lineno, newString("atl"), atl_transform));

                // TODO: double check that any atl is after any "at" keyword?
            }

            // a line can be either of the following
            // a child
            // a broken keyword
            // a list of keywords, potentially followed by an atl transform

            // accumulator for a line of keywords
            PythonObject current_keyword_line = None;

            // datastructure of (lineno, type, contents....)
            // possible types
            // "child"
            // "keywords"
            // "keywords_atl"
            // "keywords_broken"
            PythonObject contents_grouped = newList();
            for (PythonObject $args : contents_in_order) {
                PythonObject lineno = $args.getItem(0);
                PythonObject ty = $args.getItem(1);
                PythonObject content = $args.getItem(2);

                if (current_keyword_line == None) {
                    if (ty.equals("child")) {
                        contents_grouped.callAttribute("append", newTuple(lineno, newString("child"), content));
                    } else if (ty.equals("keyword")) {
                        current_keyword_line = newTuple(lineno, newString("keywords"), newList(content));
                    } else if (ty.equals("broken")) {
                        contents_grouped.callAttribute("append",
                                newTuple(lineno, newString("keywords_broken"), newList(), content));
                    } else if (ty.equals("atl")) {
                        contents_grouped.callAttribute("append", newTuple(lineno, newString("keywords_atl"), newList(), content));
                    }
                } else {
                    if (ty.equals("child")) {
                        contents_grouped.callAttribute("append", current_keyword_line);
                        current_keyword_line = None;
                        contents_grouped.callAttribute("append", newTuple(lineno, newString("child"), content));
                    } else if (ty.equals("keyword")) {
                        if( current_keyword_line.getItem(0).equals(lineno)) {
                            current_keyword_line.getItem(2).callAttribute("append", content);
                        } else {
                            contents_grouped.callAttribute("append", current_keyword_line);
                            current_keyword_line = newTuple(lineno, newString("keywords"), newTuple(content));
                        }
                    } else if (ty.equals("broken")) {
                        contents_grouped.callAttribute("append",
                            newTuple(current_keyword_line.getItem(0), newString("keywords_broken"),
                             current_keyword_line.getItem(2), content));
                        current_keyword_line = None;
                    } else if (ty.equals("atl")) {
                        if (current_keyword_line.getItem(0).equals(lineno)) {
                            contents_grouped.callAttribute("append",
                                newTuple(lineno, newString("keywords_atl"), current_keyword_line.getItem(2), content));
                            current_keyword_line = None;
                        } else {
                            contents_grouped.callAttribute("append", current_keyword_line);
                            current_keyword_line = None;
                            contents_grouped.callAttribute("append", newTuple(lineno, newString("keywords_atl"), newList(), content));
                        }
                    }

                }

            }

            if (current_keyword_line != None) {
                contents_grouped.callAttribute("append", current_keyword_line);
            }

            // We need to assign linenos to any broken keywords that don't have them. Best guess
            // is the previous lineno + 1 unless that doesn't exist, in which case it's the first
            // available line
            for (PythonObject i : range(len(contents_grouped))) {
                PythonObject lineno = contents_grouped.getItem(i).getItem(0);
                PythonObject ty = contents_grouped.getItem(i).getItem(1);

                if (ty.equals("keywords_broken") && lineno == None) {
                    PythonObject contents = contents_grouped.getItem(i).getItem(3);

                    if (i.jNotEquals(0)) {
                        lineno = contents_grouped.getItem(i.subtract(1)).getItem(0).add(1);
                    } else {
                        lineno = start_lineno;
                    }

                    contents_grouped.setItem(i, newTuple(lineno, newString("keywords_broken"), newList(), contents));
                }

            }

            // these two keywords have no lineno information with them
            // additionally, since 7.3 upwards, tag cannot be placed on the same line as `screen`
            // for whatever reason.
            // it is currently impossible to have both an `as` and a `tag` keyword in the same
            // displayble `as` is only used for displayables, `tag` for screens.
            // strategies:
            // - if there's several empty lines before any line, we can make some new lines for them
            // - if the first line is a keyword line, we can merge them with it
            // - for 'as', we can put it on the first line as well
            if (keyword_tag.toBoolean()) {
                // if there's no content, we can (strangely enough) put it on the first line,
                // because a screen without content doesn't start a block. but for sanity sake
                // we'll put it on the first line afterwards
                if (!contents_grouped.toBoolean()) {
                    contents_grouped.callAttribute("append", newTuple(block_lineno.add(1), newString("keywords"),
                            newList(newTuple(newString("tag"), keyword_tag))));
                }

                // or if the first line of the block is empty, put it there
                else if (contents_grouped.getItem(0).getItem(0).jGreaterThan(block_lineno.add(1))) {
                    contents_grouped.callAttribute("insert", newInt(0), newTuple(
                            block_lineno.add(1), newString("keywords"), newList(
                                    newTuple(newString("tag"), keyword_tag))));
                } else {
                    // try to find a line with keywords that we can merge it into
                    boolean $executeElse = true;
                    for (PythonObject entry : contents_grouped) {
                        if (entry.getItem(1).callAttributeJB("startswith", newString("keywords"))) {
                            entry.getItem(2).callAttribute("append", newTuple(newString("tag"), keyword_tag));

                            $executeElse = false;
                            break;
                        }
                    }

                    // just force it in there. this might disturb linenumbers but it's
                    // really hard to know where inbetween children it'd be safe
                    // to put it in
                    if ($executeElse)  {
                        contents_grouped.callAttribute("insert",
                            newInt(0), newTuple(
                                    block_lineno.add(1), newString("keywords"), newList(newTuple(newString("tag"), keyword_tag))));
                    }
                }
            }

            if (keyword_as.toBoolean()) {
                // if there's no content, put it on the first available line
                if (!contents_grouped.toBoolean()) {
                    contents_grouped.callAttribute("append", newTuple(start_lineno, newString("keywords"),
                            newList(newTuple(newString("as"), keyword_as))));
                }

                // or if the first line of the block is empty, put it there
                else if (contents_grouped.getItem(0).getItem(0).jGreaterThan(block_lineno.add(1))) {
                    contents_grouped.callAttribute("insert", newInt(0), newTuple(block_lineno.add(1), newString("keywords"), newList(
                            newTuple(newString("as"), keyword_as)))
                    );
                }

                // we can also put it on the start line if that one is available
                else if (contents_grouped.getItem(0).getItem(0).jGreaterThan(start_lineno)) {
                    contents_grouped.callAttribute("insert", newInt(0), newTuple(start_lineno, newString("keywords"),
                            newList(newTuple(newString("as"), keyword_as))));
                }

                else {
                    // try to find a line with keywords that we can merge it into
                    boolean $executeElse = true;
                    for (PythonObject entry : contents_grouped) {
                        if (entry.getItem(1).callAttributeJB("startswith", newString("keywords"))) {
                            entry.getItem(2).callAttribute("append", newTuple(newString("as"), keyword_as));
                            break;
                        }
                    }

                    // just force it in there. this might disturb linenumbers but it's
                    // really hard to know where inbetween children it'd be safe
                    // to put it in
                    if ($executeElse) {
                        contents_grouped.callAttribute("insert", newInt(0),
                                newTuple(start_lineno, newString("keywords"), newList(newTuple(newString("as"), keyword_as))));
                    }
                }
            }

            // if there's no content on the first line, insert an empty line, to make processing
            // easier.
            if (immediate_block.toBoolean() || !contents_grouped.toBoolean()
                    || contents_grouped.getItem(0).getItem(0).jNotEquals(block_lineno)) {
                contents_grouped.callAttribute("insert", newInt(0), newTuple(
                        block_lineno, newString("keywords"), newList()));
            }

            // return first_line_content, later_contents
            return newTuple(contents_grouped.getItem(0), contents_grouped.sliceFrom(1));
        }

        private static void
        printKeywordOrChild(PythonObject self, PythonObject item,
                               PythonObject first_line, PythonObject has_block) {
            PythonObject sep = sl2decompiler.callAttribute("First", newString(first_line.toBoolean() ? " " :""), newString(" "));

            PythonObject lineno = item.getItem(0);
            PythonObject ty = item.getItem(1);

            if (ty.equals("child")) {
                self.callAttribute("print_node", item.getItem(2));
                return;
            }

            if (!first_line.toBoolean()) {
                self.callAttribute("advance_to_line", lineno);
                self.callAttribute("indent");
            }

            for (PythonObject $args : item.getItem(2)) {
                PythonObject name = $args.getItem(0), value = $args.getItem(1);

                self.callAttribute("write", sep.call());
                self.callAttribute("write", format("{0} {1}", name, value));
            }

            if (ty.equals("keywords_atl")) {
                // TODO: assert not has_block, "cannot start a block on the same line as an at transform block"
                self.callAttribute("write", sep.call());
                self.callAttribute("write", newString("at transform:"));

                self.setAttribute("linenumber", sl2decompiler.callNestedAttribute("atldecompiler.pprint",
                    self.getAttribute("out_file"), item.getItem(3), self.getAttribute("options"),
                    self.getAttribute("indent_level"), self.getAttribute("linenumber"), self.getAttribute("skip_indent_until_write")
                ));
                self.setAttribute("skip_indent_until_write", False);
                return;
            }

            if (ty.equals("keywords_broken")) {
                self.callAttribute("write", sep.call());
                self.callAttribute("write", item.getItem(3));
            }

            if (first_line.toBoolean() && has_block.toBoolean()) {
                self.callAttribute("write", newString(":"));
            }
        }

    }

}
