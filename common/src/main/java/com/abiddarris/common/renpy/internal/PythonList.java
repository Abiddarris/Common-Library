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

import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.PythonObject.IndexError;
import static com.abiddarris.common.renpy.internal.PythonObject.StopIteration;

import java.util.List;

class PythonList extends PythonObject {

    private static PythonObject list_iterator;
    
    private List<PythonObject> elements;

    static void init() {
        list_iterator = newClass("list_iterator", newTuple(), newDict(
            newString("__next__"), newFunction(findMethod(ListIteratorObject.class, "next"), "self")
        ));
    }
    
    PythonList(List<PythonObject> elements) {
        this.elements = elements;
        
        setAttribute("__class__", list);
    }
    
    private static PythonObject getItem(PythonList list, PythonObject index) {
        int indexInt = index.toInt();
        int size = list.elements.size();

        if (indexInt < 0) {
            indexInt += size;
        }

        if(indexInt < 0 || indexInt >= size) {
            IndexError.call(newString("list index out of range")).raise();
        }
        return list.elements.get(indexInt);
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
    
    private static PythonObject iter(PythonList self) {
        return new ListIteratorObject(self);
    }
    
    private static class ListIteratorObject extends PythonObject {
        
        private int index;
        private PythonList list;
        
        private ListIteratorObject(PythonList list) {
            this.list = list;
            
            setAttribute("__class__", list_iterator);
        }
        
        private static PythonObject next(ListIteratorObject self) {
            if(self.index == self.list.elements.size()) {
                StopIteration.call().raise();
            }
            return self.list.getItem(newInt(self.index++));
        }
        
    }
}
