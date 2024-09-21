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

class PythonInt extends PythonObject {

    private long value;

    PythonInt(long value) {
        this.value = value;
    }

    private static PythonObject hash(PythonInt self) {
        return self;
    }

    private static PythonObject eq(PythonInt self, PythonObject other) {
        if (!(other instanceof PythonInt)) {
            return False;
        }
        return newBoolean(self.value == ((PythonInt)other).value);
    }

    private static PythonObject greaterThan(PythonInt self, PythonInt value) {
        return newBoolean(self.value > value.value);
    }

    private static PythonObject lessThan(PythonInt self, PythonInt value) {
        return newBoolean(self.value < value.value);
    }

    private static PythonObject add(PythonInt self, PythonInt value) {
        return newInt(self.value + value.value);
    }

    private static PythonObject subtract(PythonInt self, PythonInt value) {
        return newInt(self.value - value.value);
    }

    private static PythonObject bool(PythonInt self) {
        return newBoolean(self.value != 0);
    }

    private static PythonObject
    str(PythonInt self) {
        return newString(String.valueOf(self.value));
    }

    @Override
    public int toInt() {
        return Math.toIntExact(value);
    }
}
