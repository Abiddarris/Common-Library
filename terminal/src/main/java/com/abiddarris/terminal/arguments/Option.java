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
import com.abiddarris.terminal.arguments.validators.AlwaysAcceptValidator;
import com.abiddarris.terminal.arguments.validators.Validator;

public class Option<T> extends BaseOption<T> {

    private Validator<T> validator = new AlwaysAcceptValidator<>();

    public Option(String name, ValueParser<T> parser) {
        super(name, parser);
    }

    public Option(String name, ValueParser<T> parser, Validator<T> validator) {
        super(name, parser);

        setValidator(validator);
    }

    public Option(String name, char shortName, ValueParser<T> parser) {
        super(name, shortName, parser);
    }

    public Option(String name, char shortName, ValueParser<T> parser, Validator<T> validator) {
        super(name, shortName, parser);

        setValidator(validator);
    }

    public Option(String name, char[] shortNames, ValueParser<T> parser) {
        super(name, shortNames, parser);
    }

    public Option(String name, char[] shortNames, ValueParser<T> parser, Validator<T> validator) {
        super(name, shortNames, parser);

        setValidator(validator);
    }

    public Validator<T> getValidator() {
        return validator;
    }

    public void setValidator(Validator<T> validator) {
        checkNonNull(validator, "validator cannot be null");

        this.validator = validator;
    }

    @Override
    public void setValue(T value) {
        getValidator().validate(value);

        super.setValue(value);
    }
}
