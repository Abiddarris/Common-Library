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
package com.abiddarris.python3.builder;

import com.abiddarris.python3.PythonObject;

public class DecorateAttribute extends DelegateTarget {

    private PythonObject decorator;

    public DecorateAttribute(Target target, PythonObject decorator) {
        super(target);

        this.decorator = decorator;
    }

    @Override
    public void onDefine(String name, PythonObject object) {
        super.onDefine(name, decorator.call(object));
    }
}
