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

class MultipleValuesArgument<T> {

    final List<T> values = new ArrayList<T>();

    MultipleValuesArgument() {
    }

    public void setValue(T value) {
        values.set(0, value);
    }

    public T getValue() {
        return !values.isEmpty() ? values.get(0) : null;
    }

    protected void parse(String arg) {
        throw new UnsupportedOperationException();
    }

    protected <Arg extends Argument<T> & HasValidator<T>> void parse(Arg argument, String[] args) {
        List<T> parsed = new ArrayList<T>();
        ValueParser<T> parser = argument.getParser();
        for (String arg : args) {
            parsed.add(parser.parse(arg));
        }

        setValues(argument, parsed);
    }

    public List<T> getValues() {
        return Collections.unmodifiableList(values);
    }

    public void setValues(HasValidator<T> argument, List<T> values) {
        if (values == null) {
            this.values.clear();
            return;
        }

        Validator<T> validator = argument.getValidator();
        for (T value : values) {
            validator.validate(value);
        }

        this.values.clear();
        this.values.addAll(values);
    }

    public void setValues(HasValidator<T> argument, T[] values) {
        setValues(argument, Arrays.asList(values));
    }
}