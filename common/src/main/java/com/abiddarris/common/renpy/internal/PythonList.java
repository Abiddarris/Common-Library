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

import static com.abiddarris.common.renpy.internal.Builtins.IndexError;
import static com.abiddarris.common.renpy.internal.Builtins.None;
import static com.abiddarris.common.renpy.internal.Builtins.StopIteration;
import static com.abiddarris.common.renpy.internal.Builtins.slice;
import static com.abiddarris.common.renpy.internal.core.JFunctions.jIsinstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PythonList extends PythonObject {

    private static PythonObject list_iterator;
    
    private List<PythonObject> elements;

    static void init() {
        list_iterator = newClass("list_iterator", newTuple(), newDict(
            newString("__next__"), newFunction(findMethod(ListIteratorObject.class, "next"), "self")
        ));
    }
    
    PythonList(PythonObject cls, List<PythonObject> elements) {
        this.elements = elements;
        
        setAttributeDirectly("__class__", cls);
    }


    static PythonObject new0(PythonObject cls, PythonObject iterable) {
        return new PythonList(cls, new ArrayList<>());
    }

    static void init0(PythonObject self, PythonObject iterable) {
        PythonList list = (PythonList) self;
        for (PythonObject element : iterable) {
            list.elements.add(element);
        }
    }

    private static PythonObject getItem(PythonList list, PythonObject index) {
        if (jIsinstance(index, slice)) {
            PythonObject pStart = index.getAttribute("start");
            PythonObject pStop = index.getAttribute("stop");
            PythonObject pStep = index.getAttribute("step");

            int start = pStart == None ? 0 : pStart.toInt();
            int end = pStop == None ? list.elements.size() : pStop.toInt();
            int step = pStep == None ? 1 : pStep.toInt();

            start = getRawIndex(list, start);
            end = getRawIndex(list, end);

            List<PythonObject> items = new ArrayList<>();
            for (int i = start; i < end; i += step) {
                items.add(list.elements.get(i));
            }
            return newList(items);
        }
        int indexInt = getRawIndex(list, index);

        if(indexInt < 0 || indexInt >= list.elements.size()) {
            IndexError.call(newString("list index out of range")).raise();
        }
        return list.elements.get(indexInt);
    }

    private static void setItem(PythonList self, PythonObject key, PythonObject value) {
        int index = getRawIndex(self, key);

        if(index < 0 || index >= self.elements.size()) {
            IndexError.call(newString("list assignment index out of range")).raise();
        }

        self.elements.set(index, value);
    }
    
    private static void insert(PythonList list, PythonObject index, PythonObject element) {
        int indexInt = index.toInt();
        if(indexInt < 0 || indexInt > list.elements.size()) {
            list.elements.add(element);
            return;
        }
        
        list.elements.add(indexInt, element);
    }
    
    private static void append(PythonList self, PythonObject element) {
        self.elements.add(element);
    }

    static void
    extend(PythonObject self, PythonObject iterable) {
        PythonList list = (PythonList) self;
        for (PythonObject element : iterable) {
            list.elements.add(element);
        }
    }
    
    private static PythonObject iter(PythonList self) {
        return new ListIteratorObject(self);
    }

    private static PythonObject pop(PythonList self) {
        List<PythonObject> elements = self.elements;
        int size = elements.size();
        if (size == 0) {
            IndexError.call(newString("pop index out of range"));
        }

        return elements.remove(size - 1);
    }

    private static PythonObject len(PythonList self) {
        return newInt(self.elements.size());
    }

    private static int getRawIndex(PythonList self, PythonObject index) {
        int indexInt = index.toInt();
        return getRawIndex(self, indexInt);
    }

    private static int getRawIndex(PythonList self, int indexInt) {
        int size = self.elements.size();

        if (indexInt < 0) {
            indexInt += size;
        }

        return indexInt;
    }

    static PythonObject
    contains(PythonObject self, PythonObject value) {
        return newBoolean(((PythonList)self).elements.contains(value));
    }

    static void
    reverse(PythonObject self) {
        Collections.reverse(((PythonList)self).elements);
    }

    private static class ListIteratorObject extends PythonObject {
        
        private int index;
        private PythonList list;
        
        private ListIteratorObject(PythonList list) {
            this.list = list;
            
            setAttributeDirectly("__class__", list_iterator);
        }
        
        private static PythonObject next(ListIteratorObject self) {
            if(self.index == self.list.elements.size()) {
                StopIteration.call().raise();
            }
            return self.list.getItem(newInt(self.index++));
        }
        
    }
}
