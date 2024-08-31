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
 ***********************************************************************************/
package com.abiddarris.common.renpy.internal;

import com.abiddarris.common.renpy.internal.model.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;

import java.util.Arrays;

public class PythonTuple extends PythonObject {

    private static PythonObject tuple_iterator;

    static void init() {
        tuple_iterator = type.callAttribute("__new__",
            new PythonArgument()
                .addPositionalArgument(type)
                .addPositionalArgument(newString("tuple_iterator"))
                .addPositionalArgument(newTuple(object))
                .addPositionalArgument(newDict()));
        tuple_iterator.setAttribute("__next__", newFunction(
            findMethod(TupleIterator.class, "next"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()));
    }

    PythonObject[] elements;

    PythonTuple(PythonObject[] elements) {
        super(new BootstrapAttributeHolder());
        
        this.elements = elements;
    }
    
    public PythonObject[] getElements() {
        return elements;
    }

    private static PythonObject getitem(PythonObject self, PythonObject pos) {
        if (!(self instanceof PythonTuple)) {
            throw new IllegalArgumentException("Error");
        }

        return ((PythonTuple) self).elements[unpackPythonInt(pos)];
    }

    private static PythonObject iter(PythonTuple self) {
        TupleIterator iterator = new TupleIterator(self);
        iterator.setAttribute("__class__", tuple_iterator);

        return iterator;
    }

    private static PythonObject len(PythonTuple self) {
        return newInt(self.elements.length);
    }
    
    private static PythonObject str(PythonTuple self) {
        String jString = Arrays.toString(self.elements);
        jString = jString.substring(1, jString.length() - 1);
        jString = "(" + jString + ")";
        
        return newString(jString);
    }

    private static class TupleIterator extends PythonObject {

        private int index;
        private PythonTuple tuple;

        private TupleIterator(PythonTuple tuple) {
            super(new BootstrapAttributeHolder());
            
            this.tuple = tuple;
        }

        private static PythonObject next(TupleIterator self) {
            PythonObject[] elements = self.tuple.elements;
            if (elements.length == self.index) {
                StopIteration.callAttribute("__new__", new PythonArgument()
                    .addPositionalArgument(StopIteration))
                    .raise();
            }

            return elements[self.index++];
        }
    }
}
