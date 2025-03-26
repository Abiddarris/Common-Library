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
package com.abiddarris.terminal.arguments.parsers;

import com.abiddarris.terminal.arguments.ArgumentParserException;

public class BooleanParser implements ValueParser<Boolean> {

    private final boolean allowInt;

    public static final BooleanParser INSTANCE = new BooleanParser();
    public static final BooleanParser INSTANCE_ALLOW_INT = new BooleanParser(true);

    public BooleanParser() {
        this(false);
    }

    public BooleanParser(boolean allowInt) {
        this.allowInt = allowInt;
    }

    @Override
    public Boolean parse(String val) {
        val = val.toLowerCase();
        if (val.equals("false")) {
            return false;
        }

        if (val.equals("true")) {
            return true;
        }

        if (!allowInt) {
            throw new ArgumentParserException("Expected true or false but got " + val);
        }

        if (val.equals("0")) {
            return false;
        }

        if (val.equals("1")) {
            return true;
        }

        throw new ArgumentParserException("Expected true or false or 0 or 1 but got " + val);
    }
}
