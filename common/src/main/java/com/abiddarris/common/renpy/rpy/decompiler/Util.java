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
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

public class Util {
    
    static void initLoader() {
        registerLoader("decompiler.util", (name) -> {
            PythonObject util = createModule(name);
            PythonObject OptionBase = OptionBaseImpl.define(util);  
            
            DecompilerBaseImpl.define(util, OptionBase);
                
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
        
        private static PythonObject define(PythonObject util, PythonObject OptionBase) {
            ClassDefiner definer = util.defineClass("DecompilerBase");
            definer.defineFunction("__init__", DecompilerBaseImpl.class, "init", new PythonSignatureBuilder("self")
                .addParameter("out_file", None)
                .addParameter("options", OptionBase.call())
                .build());
            
            return definer.define();
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
    }
    
}
