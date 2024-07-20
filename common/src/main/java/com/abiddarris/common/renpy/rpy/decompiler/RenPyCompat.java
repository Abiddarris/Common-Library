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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This module holds some special classes and shorthand functions for support of renpy compatiblity.
 * They're separate so there will be less code duplication, simpler dependencies between files and
 * to avoid middle-of-file imports.
 */
public class RenPyCompat {
    
    public static final Magic.FakeClassFactory CLASS_FACTORY = new Magic.FakeClassFactory(Collections.EMPTY_LIST,/*SPECIAL_CLASSES*/ Magic.FakeStrict);

    public static Object pickle_safe_loads(int[] buffer) {
        return Magic.safe_loads(buffer, CLASS_FACTORY,
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
