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

import com.abiddarris.common.renpy.internal.PythonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeneratorObject extends PythonObject {

    private List<ForEachStatement> forEachStatements;
    private Variables var = new Variables();
    private Yield yield;
    private boolean first = true;

    public GeneratorObject(List<ForEachStatement> forEachStatements, Yield yield) {
        super(builtins.getAttribute("generator"));

        this.forEachStatements = forEachStatements;
        this.yield = yield;
    }

    public PythonObject next() {
        if (first) {
            first = false;

            return executeFrom(0);
        }

        for (int i = forEachStatements.size() - 1; i >= 0; i--) {
            ForEachStatement statement = forEachStatements.get(i);
            if (statement.execute(var)) {
                return executeFrom(i + 1);
            }
        }

        return null;
    }

    private PythonObject executeFrom(int index) {
        for (int i = index; i < forEachStatements.size(); i++) {
            ForEachStatement statement = forEachStatements.get(i);
            if (!statement.execute(var)) {
                return null;
            }
        }

        return yield.apply(var);
    }

}
