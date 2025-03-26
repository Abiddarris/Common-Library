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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseOption<T> extends Argument<T> {

    private final char[] shortNames;

    protected BaseOption(String name, ValueParser<T> parser) {
        this(name, null, parser);
    }

    protected BaseOption(String name, char shortName, ValueParser<T> parser) {
        this(name, new char[] {shortName}, parser);
    }

    protected BaseOption(String name, char[] shortNames, ValueParser<T> parser) {
        super(name, parser);

        List<Character> shortNamesList = new ArrayList<>();
        if (shortNames != null) {
            for (char shortName : shortNames) {
                if (!shortNamesList.contains(shortName)) {
                    shortNamesList.add(shortName);
                }
            }
        }

        this.shortNames = new char[shortNamesList.size()];
        for (int i = 0; i < this.shortNames.length; i++) {
            this.shortNames[i] =  shortNamesList.get(i);
        }
    }

    public char[] getShortNames() {
        return Arrays.copyOf(shortNames, shortNames.length);
    }
}
