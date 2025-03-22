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
package com.abiddarris.terminal.arguments;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import com.abiddarris.terminal.arguments.parsers.ValueParser;

public abstract class Argument<T> {

    private final String name;
    private final ValueParser<T> parser;

    private T value;

    public Argument(String name, ValueParser<T> parser) {
        checkNonNull(name, "name cannot be null");
        checkNonNull(parser, "parser cannot be null");

        this.name = name;
        this.parser = parser;
    }

    public String getName() {
        return name;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public ValueParser<T> getParser() {
        return parser;
    }

    protected void parse(String arg) {
        setValue(getParser().parse(arg));
    }
}
