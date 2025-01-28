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
package com.abiddarris.python3.imp;

import com.abiddarris.python3.PythonObject;

import java.util.Map;

public class ImportAsTarget extends DelegateLoadTarget {

    private Map<String, String> map;

    public ImportAsTarget(LoadTarget target, Map<String, String> map) {
        super(target);

        this.map = map;
    }

    @Override
    public void onImport(String name, PythonObject importedAttribute) {
        super.onImport(map.get(name), importedAttribute);
    }
}
