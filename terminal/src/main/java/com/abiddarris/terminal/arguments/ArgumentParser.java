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

import java.util.ArrayList;
import java.util.List;

public class ArgumentParser {

    private final List<PositionalArgument<?>> positionalArguments = new ArrayList<>();

    public void require(PositionalArgument<?> argument) {
        positionalArguments.add(argument);
    }

    public void parse(String[] args) {
        if (args.length > positionalArguments.size()) {
            int pos = positionalArguments.size() + 1;
            throw new ArgumentParserException(
                    String.format("Unexpected %s argument", pos + getSuffix(pos))
            );
        }

        for (int i = 0; i < positionalArguments.size(); i++) {
            if (i == args.length) {
                throw new ArgumentParserException(String.format("Missing %s argument", (i + 1) + getSuffix(i + 1)));
            }

            PositionalArgument<?> positionalArgument = positionalArguments.get(i);
            positionalArgument.parse(args[i]);
        }
    }

    private String getSuffix(int length) {
        String lengthStr = String.valueOf(length);
        if (lengthStr.endsWith("1")) {
            return "st";
        } else if (lengthStr.endsWith("2")) {
            return "nd";
        } else if (lengthStr.endsWith("3")) {
            return "rd";
        }

        return "th";
    }
}
