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
package com.abiddarris.common.renpy.rpy.decompiler;

import com.abiddarris.common.renpy.internal.loader.JavaModuleLoader;

public class Decompiler {
    
    public static void initLoader() {
        JavaModuleLoader.registerPackageLoader("decompiler", (decompiler) -> {
            decompiler.importModule("decompiler.util");
        });
        Magic.initLoader();
        RenPyCompat.initLoader();
        Util.initLoader();
    }
    
}
