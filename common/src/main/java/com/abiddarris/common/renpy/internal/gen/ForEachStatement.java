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
package com.abiddarris.common.renpy.internal.gen;

import static com.abiddarris.common.renpy.internal.Python.tryExcept;
import static com.abiddarris.common.renpy.internal.PythonObject.StopIteration;

import com.abiddarris.common.renpy.internal.PythonObject;
import com.abiddarris.common.utils.ObjectWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class ForEachStatement {

    private IteratorSupplier iteratorSupplier;
    private List<Filter> filters = new ArrayList<>();
    private NameSetter nameSetter;
    private PythonObject iterator;
    private ObjectWrapper<PythonObject> obj = new ObjectWrapper<>(null);

    ForEachStatement(IteratorSupplier iteratorSupplier, NameSetter nameSetter) {
        this.iteratorSupplier = iteratorSupplier;
        this.nameSetter = nameSetter;
    }

    public boolean execute(Variables vars) {
        if (iterator == null) {
            iterator = iteratorSupplier.apply(vars);
            iterator = iterator.callAttribute("__iter__");
        }

        while (true) {
            tryExcept(() -> obj.setObject(iterator.callAttribute("__next__")))
                    .onExcept((e) -> obj.setObject(null), StopIteration)
                    .execute();

            PythonObject obj = this.obj.getObject();
            if (obj == null) {
                iterator = null;

                return false;
            }

            nameSetter.accept(vars, obj);

            if (runFilters(vars)) {
                return true;
            }
        }
    }

    void addFilter(Filter filter) {
        filters.add(filter);
    }

    private boolean runFilters(Variables vars) {
        for (Filter filter : filters) {
            if (!filter.apply(vars).toBoolean()) {
                return false;
            }
        }
        return true;
    }
}
