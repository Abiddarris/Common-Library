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

import static com.abiddarris.common.renpy.internal.Builtins.ValueError;
import static com.abiddarris.common.renpy.internal.Python.newString;

import com.abiddarris.common.renpy.internal.PythonObject;

import java.util.ArrayList;
import java.util.List;

public class GeneratorBuilder {

    private boolean invalid;
    private List<ForEachStatement> forEachStatements = new ArrayList<>();

    GeneratorBuilder() {
    }

    public ForEachBuilder forEach(IteratorSupplier supplier) {
        checkValid();
        invalid = true;

        return new ForEachBuilder(this, supplier);
    }

    public GeneratorBuilder forEach(IteratorSupplier supplier, String... varNames) {
        return forEach(supplier)
                .name((vars, var) -> {
                    if (varNames.length == 0) {
                        return;
                    }

                    if (varNames.length == 1) {
                        vars.put(varNames[0], var);
                        return;
                    }

                    int value = 0;
                    for (PythonObject var0 : var) {
                        vars.put(varNames[value++], var0);
                    }

                    if (value < varNames.length) {
                        ValueError.call(newString(String.format(
                                "not enough values to unpack (expected %s, got %s)", varNames.length, value
                        ))).raise();
                    }
                });
    }

    public GeneratorBuilder forEach(IteratorSupplierNoVar supplier, String... varNames) {
        return forEach(vars -> supplier.getIterator(), varNames);
    }

    public GeneratorBuilder filter(Filter filter) {
        checkValid();

        if (forEachStatements.size() == 0) {
            throw new IllegalStateException("filter not preceded by foreach");
        }

        forEachStatements.get(forEachStatements.size() - 1)
                .addFilter(filter);

        return this;
    }

    public PythonObject yield(Yield yield) {
        checkValid();
        invalid = true;

        return new GeneratorObject(forEachStatements, yield);
    }

    private void checkValid() {
        if (invalid) {
            throw new IllegalStateException("Attempt to call function in invalid state");
        }
    }

    void addForEach(ForEachStatement forEachStatement) {
        forEachStatements.add(forEachStatement);

        invalid = false;
    }
}
