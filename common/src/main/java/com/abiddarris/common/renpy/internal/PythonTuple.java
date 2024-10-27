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

import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Builtins.hash;
import static com.abiddarris.common.renpy.internal.Builtins.tuple;

import com.abiddarris.common.renpy.internal.attributes.BootstrapAttributeHolder;
import com.abiddarris.common.renpy.internal.signature.PythonArgument;
import com.abiddarris.common.renpy.internal.signature.PythonSignatureBuilder;
import com.abiddarris.common.renpy.internal.utils.ArrayIterator;

import java.util.Arrays;
import java.util.Iterator;

public class PythonTuple extends PythonObject {

    private static PythonObject tuple_iterator;

    static void init2() {
        tuple.defineAttribute("__module__", newString("builtins"));
        tuple.defineFunction("__eq__", PythonTuple::eq, "self", "other");
        tuple.defineFunction("__hash__", PythonTuple::hash, "self");
    }

    static void init() {
        tuple_iterator = Bootstrap.newClass(Builtins.type, newTuple(newString("tuple_iterator"), newTuple(Builtins.object)), new BootstrapAttributeHolder());
        tuple_iterator.setAttribute("__next__", newFunction(
            findMethod(TupleIterator.class, "next"),
            new PythonSignatureBuilder()
                .addParameter("self")
                .build()));
    }

    PythonObject[] elements;

    private boolean useJavaIterator;

    PythonTuple(PythonObject cls, PythonObject[] elements) {
        super(new BootstrapAttributeHolder(), cls);

        this.useJavaIterator = cls == tuple;
        this.elements = elements;
    }
    
    public PythonObject[] getElements() {
        return elements;
    }

    @Override
    public Iterator<PythonObject> iterator() {
        if (useJavaIterator) {
            return new ArrayIterator(elements);
        }
        return super.iterator();
    }

    private static PythonObject eq(PythonObject self, PythonObject other) {
        if (!(other instanceof PythonTuple)) {
            return False;
        }

        return newBoolean(Arrays.equals(
                ((PythonTuple)self).elements,
                ((PythonTuple)other).elements
        ));
    }

    private static PythonObject hash(PythonObject self) {
        PythonObject hash0 = newInt(0);
        PythonTuple tuple = (PythonTuple)self;

        for (PythonObject object : tuple.elements) {
            hash0.multiply(31)
                    .add(hash.call(object));
        }
        return hash0;
    }

    private static PythonObject getitem(PythonTuple self, PythonObject pos) {
        int indexInt = pos.toInt();
        int size = self.elements.length;

        if (indexInt < 0) {
            indexInt += size;
        }

        if(indexInt < 0 || indexInt >= size) {
            Builtins.IndexError.call(newString("tuple index out of range")).raise();
        }

        return self.elements[indexInt];
    }

    private static PythonObject iter(PythonTuple self) {
        TupleIterator iterator = new TupleIterator(self);
        iterator.setAttributeDirectly("__class__", tuple_iterator);

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

    private static PythonObject contains(PythonTuple self, PythonObject value) {
        for (PythonObject element : self) {
            if (element.equals(value)) {
                return Builtins.True;
            }
        }
        return False;
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
                Builtins.StopIteration.callAttribute("__new__", new PythonArgument()
                    .addPositionalArgument(Builtins.StopIteration))
                    .raise();
            }

            return elements[self.index++];
        }
    }
}
