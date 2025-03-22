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

import com.abiddarris.terminal.arguments.parsers.ValueParser;
import com.abiddarris.terminal.arguments.validators.Validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnlimitedPositionalArgument<T> extends PositionalArgument<T> {

    private final List<T> values = new ArrayList<>();

    public UnlimitedPositionalArgument(String name, ValueParser<T> parser) {
        super(name, parser);
    }

    public UnlimitedPositionalArgument(String name, ValueParser<T> parser, Validator<T> validator) {
        super(name, parser, validator);
    }

    @Override
    public void setValue(T value) {
        values.set(0, value);
    }

    @Override
    public T getValue() {
        return !values.isEmpty() ? values.get(0) : null;
    }

    @Override
    protected void parse(String arg) {
        throw new UnsupportedOperationException();
    }

    protected void parse(String[] args) {
        List<T> parsed = new ArrayList<>();
        ValueParser<T> parser = getParser();
        for (String arg : args) {
            parsed.add(parser.parse(arg));
        }

        setValues(parsed);
    }

    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void setValues(List<T> values) {
        if (values == null) {
            this.values.clear();
            return;
        }

        Validator<T> validator = getValidator();
        for (T value : values) {
            validator.validate(value);
        }

        this.values.clear();
        this.values.addAll(values);
    }

    public void setValues(T[] values) {
        setValues(Arrays.asList(values));
    }

}
