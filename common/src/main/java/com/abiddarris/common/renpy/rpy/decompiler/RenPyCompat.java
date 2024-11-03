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
 * Copyright (c) 2015-2024 CensoredUsername
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ************************************************************************************/
package com.abiddarris.common.renpy.rpy.decompiler;

import static com.abiddarris.common.renpy.internal.Builtins.list;
import static com.abiddarris.common.renpy.internal.PythonObject.*;
import static com.abiddarris.common.renpy.internal.loader.JavaModuleLoader.registerLoader;

import com.abiddarris.common.renpy.internal.Builtins;
import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.renpy.internal.builder.ClassDefiner;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This module holds some special classes and shorthand functions for support of renpy compatiblity.
 * They're separate so there will be less code duplication, simpler dependencies between files and
 * to avoid middle-of-file imports.
 */
public class RenPyCompat {

    private static PythonObject renpycompat;

    static void initLoader() {
        registerLoader("decompiler.renpycompat", (name) -> {
            renpycompat = createModule(name);
            PythonObject magic = renpycompat.fromImport("decompiler", "magic")[0];

            magic.callAttribute("fake_package", newString("renpy"));

            renpycompat.importModule("renpy");

            PythonObject SPECIAL_CLASSES = renpycompat.addNewAttribute("SPECIAL_CLASSES", newList());
            SPECIAL_CLASSES.callAttribute("append", PyExprImpl.define(renpycompat, magic));
                
            renpycompat.setAttribute("PyExpr", Builtins.None);
                
            SPECIAL_CLASSES.callAttribute("append", PyCodeImpl.define(renpycompat, magic));
         
            renpycompat.setAttribute("PyCode", Builtins.None);
                
            SPECIAL_CLASSES.callAttribute("append", RevertableDictOldImpl.define(renpycompat, magic));
         
            renpycompat.setAttribute("RevertableDict", Builtins.None);

            // These appear in the parsed contents of user statements.
            RevertableListImpl.define();

            // Before ren'py 7.5/8.0 they lived in renpy.python, so for compatibility we keep it here.
            RevertableListImpl1.define();

            return renpycompat;
        });
    }
    
    private static class PyExprImpl {
        
        private static PythonObject define(PythonObject renpycompat, PythonObject magic) {
            ClassDefiner definer = renpycompat.defineClass("PyExpr", magic.getAttribute("FakeStrict"), Builtins.str);
            definer.defineAttribute("__module__", newString("renpy.ast"));
            definer.defineFunction("__new__", PyExprImpl.class, "new0", new PythonSignatureBuilder("cls", "s", "filename", "linenumber")
                .addParameter("py", Builtins.None)
                .build());
            
            return definer.define();
        }
        
        /*
        def __getnewargs__(self):
            if self.py is not None:
                return str(self), self.filename, self.linenumber, self.py
            else:
                return str(self), self.filename, self.linenumber
        */
        
        private static PythonObject new0(PythonObject cls, PythonObject s, PythonObject filename, PythonObject linenumber, PythonObject py) {
            PythonObject self = Builtins.str.callAttribute("__new__", cls, s);
            self.setAttribute("filename", filename);
            self.setAttribute("linenumber", linenumber);
            self.setAttribute("py", py);
            
            return self;
        }
    }
    
    private static class PyCodeImpl {
        
        private static PythonObject define(PythonObject renpycompat, PythonObject magic) {
            ClassDefiner definer = renpycompat.defineClass("PyCode", magic.getAttribute("FakeStrict"));
            definer.defineAttribute("__module__", newString("renpy.ast"));
            definer.defineFunction("__setstate__", PyCodeImpl.class, "setState", "self", "state");
            
            return definer.define();
        }

        private static void setState(PythonObject self, PythonObject state) {
            if (Builtins.len.call(state).toInt() == 4) {
                self.setAttribute("source", state.getItem(newInt(1)));
                self.setAttribute("location", state.getItem(newInt(2)));
                self.setAttribute("mode", state.getItem(newInt(3)));
                self.setAttribute("py", Builtins.None);
            } else {
                self.setAttribute("source", state.getItem(newInt(1)));
                self.setAttribute("location", state.getItem(newInt(2)));
                self.setAttribute("mode", state.getItem(newInt(3)));
                self.setAttribute("py", state.getItem(newInt(4)));
            }
            self.setAttribute("bytecode", Builtins.None);
        }
    }      

    private static class RevertableListImpl {

        private static void define() {
            ClassDefiner definer = renpycompat.defineDecoratedClass("RevertableList",
                    renpycompat.getNestedAttribute("SPECIAL_CLASSES.append"),
                    renpycompat.getNestedAttribute("magic.FakeStrict"), list);
            definer.defineAttribute("__module__", newString("renpy.revertable"));
        }

        private static PythonObject new0(PythonObject cls) {
            return list.callAttribute("__new__", cls);
        }

    }

    private static class RevertableListImpl1 {

        private static void define() {
            ClassDefiner definer = renpycompat.defineDecoratedClass("RevertableList",
                    renpycompat.getNestedAttribute("SPECIAL_CLASSES.append"),
                    renpycompat.getNestedAttribute("magic.FakeStrict"), list);

            definer.defineAttribute("__module__", newString("renpy.python"));
            definer.defineFunction("__new__", RevertableListImpl1::new0, "cls");
            definer.define();
        }

        private static PythonObject new0(PythonObject cls) {
            return list.callAttribute("__new__", cls);
        }

    }

    private static class RevertableDictOldImpl {
       
        private static PythonObject define(PythonObject renpycompat, PythonObject magic) {
            ClassDefiner definer = renpycompat.defineClass("RevertableDict", magic.getAttribute("FakeStrict"), Builtins.dict);
            definer.defineAttribute("__module__", newString("renpy.python"));
            definer.defineFunction("__new__", RevertableDictOldImpl.class, "new0", "cls");
            
            return definer.define();
        }

        private static PythonObject new0(PythonObject cls) {
            return Builtins.dict.callAttribute("__new__", cls);
        }
    }

    private static Magic.FakeClassFactory CLASS_FACTORY;
    
    private static Magic.FakeClassFactory getClassFactory() {
        if(CLASS_FACTORY != null) {
            return CLASS_FACTORY;
        }
        
        PythonObject renpycompat = Builtins.__import__.call(newString("decompiler.renpycompat")).getAttribute("renpycompat");
        List<PythonObject> SPECIAL_CLASSES = new ArrayList<>();
        for(PythonObject clazz : renpycompat.getAttribute("SPECIAL_CLASSES")) {
        	SPECIAL_CLASSES.add(clazz);
        }
        
        CLASS_FACTORY = new Magic.FakeClassFactory(SPECIAL_CLASSES, renpycompat.getAttribute("magic")
                                                                        .getAttribute("FakeStrict"));
        return CLASS_FACTORY;
    }
    
    public static Object pickle_safe_loads(int[] buffer) {
        return Magic.safe_loads(buffer, getClassFactory(),
             new HashSet<>(Set.of("collections")), false, "ASCII", "strict");
    }
    
    /**
     * When objects get pickled in protocol 2, python 2 will
     * normally emit BINSTRING/SHORT_BINSTRING opcodes for any attribute
     * names / binary strings.
     * protocol 2 in python 3 however, will never use BINSTRING/SHORT_BINSTRING
     * so presence of these opcodes is a tell that this file was not from renpy 8
     * even when recording a bytestring in python 3, it will not use BINSTRING/SHORT_BINSTRING
     * instead choosing to encode it into a BINUNICODE object
     *
     * caveat:
     * if a file uses `from __future__ import unicode_literals`
     * combined with __slots__ that are entered as plain "strings"
     * then attributes will use BINUNICODE instead (like py3)
     * Most ren'py AST classes do use __slots__ so that's a bit annoying
     */
    public static boolean pickle_detect_python2(int[] buffer) {
        /*for opcode, arg, pos in pickletools.genops(buffer):
            if opcode.code == "\x80":
                # from what I know ren'py for now always uses protocol 2,
                # but it might've been different in the past, and change in the future
                if arg < 2:
                    return True

                elif arg > 2:
                    return False

            if opcode.code in "TU":
                return True
        */
        return false;
    }
    
}
