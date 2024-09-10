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

import static com.abiddarris.common.renpy.internal.Python.newBoolean;
import static com.abiddarris.common.renpy.internal.Python.newInt;

class PythonString extends PythonObject {

    private String string;

    PythonString(String string) {
        this.string = string;
    }
    
    private static PythonObject contains(PythonString self, PythonObject key) {
        return newBoolean(self.string.contains(key.toString()));
    }

    private static PythonObject stringHash(PythonString self) {
        return newInt(self.string.hashCode());
    }

    private static PythonObject stringEq(PythonString self, PythonObject eq) {
        if (!(eq instanceof PythonString)) {
            return False;
        }

        return newBoolean(self.string.equals(((PythonString) eq).string));
    }

    @Override
    public String toString() {
        return string;
    }
}
