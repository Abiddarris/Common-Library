/***********************************************************************************
 * Copyright 2024-2025 Abiddarris
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
package com.abiddarris.terminal.arguments.validators;

import com.abiddarris.terminal.arguments.ArgumentParserException;

public class PermittedValueValidator<T> implements Validator<T> {

    private final T[] values;

    @SafeVarargs
    public PermittedValueValidator(T... values) {
        this.values = values;
    }

    @Override
    public void validate(T t) {
        for (T value : values) {
            if (t == null && value == null) {
                return;
            }

            if (t == null) {
                continue;
            }

            if (t.equals(value)) {
                return;
            }
        }

        throw new ArgumentParserException(String.format("Unknown value '%s' (expected %s)", t, expectedToString()));
    }

    private String expectedToString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            builder.append(values[i]);

            if (i != values.length - 1) {
                builder.append(" or ");
            }
        }

        return builder.toString();
    }
}
