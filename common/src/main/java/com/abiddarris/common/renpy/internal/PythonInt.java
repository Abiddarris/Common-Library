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

    private int value;

    PythonInt(int value) {
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

    @Override
    public int toInt() {
        return value;
    }
}
