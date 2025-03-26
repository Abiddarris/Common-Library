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

import java.util.List;

public class MultipleValueOption<T> extends Option<T> {

    private final MultipleValuesArgument<T> multipleValuesArgument = new MultipleValuesArgument<T>();

    public MultipleValueOption(String name, ValueParser<T> parser) {
        super(name, parser);
    }

    public MultipleValueOption(String name, ValueParser<T> parser, Validator<T> validator) {
        super(name, parser, validator);
    }

    public MultipleValueOption(String name, char shortName, ValueParser<T> parser) {
        super(name, shortName, parser);
    }

    public MultipleValueOption(String name, char shortName, ValueParser<T> parser, Validator<T> validator) {
        super(name, shortName, parser, validator);
    }

    public MultipleValueOption(String name, char[] shortNames, ValueParser<T> parser) {
        super(name, shortNames, parser);
    }

    public MultipleValueOption(String name, char[] shortNames, ValueParser<T> parser, Validator<T> validator) {
        super(name, shortNames, parser, validator);
    }

    @Override
    public void setValue(T value) {
        multipleValuesArgument.setValue(value);
    }

    @Override
    public T getValue() {
        return multipleValuesArgument.getValue();
    }

    @Override
    protected void parse(String arg) {
        multipleValuesArgument.parse(arg);
    }

    protected void parse(String[] args) {
        multipleValuesArgument.parse(this, args);
    }

    public List<T> getValues() {
        return multipleValuesArgument.getValues();
    }

    public void setValues(List<T> values) {
        multipleValuesArgument.setValues(this, values);
    }

    public void setValues(T[] values) {
        multipleValuesArgument.setValues(this, values);
    }

}
