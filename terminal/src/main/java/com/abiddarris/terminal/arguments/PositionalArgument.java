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

public class PositionalArgument<T> extends Argument<T> implements HasValidator<T> {

    private Validator<T> validator;

    public PositionalArgument(String name, ValueParser<T> parser) {
        this(name, parser, new AlwaysAcceptValidator<>());
    }

    public PositionalArgument(String name, ValueParser<T> parser, Validator<T> validator) {
        super(name, parser);

        setValidator(validator);
    }

    @Override
    public void setValidator(Validator<T> validator) {
        checkNonNull(validator, "validator cannot be null");

        this.validator = validator;
    }

    @Override
    public Validator<T> getValidator() {
        return validator;
    }

    @Override
    public void setValue(T value) {
        getValidator().validate(value);

        super.setValue(value);
    }
}
