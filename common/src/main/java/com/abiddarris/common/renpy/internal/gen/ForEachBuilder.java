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

public class ForEachBuilder {

    private final GeneratorBuilder builder;
    private final IteratorSupplier supplier;

    ForEachBuilder(GeneratorBuilder builder, IteratorSupplier supplier) {
        this.builder = builder;
        this.supplier = supplier;
    }

    public GeneratorBuilder name(NameSetter nameSetter) {
        builder.addForEach(new ForEachStatement(supplier, nameSetter));

        return builder;
    }
}
