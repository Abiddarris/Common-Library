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
import java.util.Arrays;
import java.util.List;

public class ArgumentParser {

    private final List<PositionalArgumentData> positionalArguments = new ArrayList<>();
    private boolean allowRequiredForPositionalArgument = true;
    private boolean allowUnlimitedPositionalArgument = true;

    public void require(PositionalArgument<?> argument) {
        if (!allowRequiredForPositionalArgument) {
            throw new IllegalStateException("Cannot add required positional argument after optional positional argument");
        }

        checkItIsUnlimitedPositionalArgument(argument);
        throwIfArgumentAlreadyAdded(argument);

        if (argument.getValue() != null) {
            argument.setValue(null);
        }

        positionalArguments.add(new PositionalArgumentData(argument, true));
    }

    public void optional(PositionalArgument<?> argument) {
        throwIfArgumentAlreadyAdded(argument);
        checkOptionalArgumentAllowed();
        checkItIsUnlimitedPositionalArgument(argument);

        if (!(argument instanceof UnlimitedPositionalArgument<?>)) {
            allowRequiredForPositionalArgument = false;
        }

        positionalArguments.add(new PositionalArgumentData(argument, false));
    }

    private void checkOptionalArgumentAllowed() {
        if (!allowUnlimitedPositionalArgument) {
            throw new IllegalArgumentException("Cannot add optional argument after unlimited positional argument");
        }
    }

    private void checkItIsUnlimitedPositionalArgument(PositionalArgument<?> argument) {
        if (argument instanceof UnlimitedPositionalArgument<?>) {
            if (!allowUnlimitedPositionalArgument) {
                throw new IllegalArgumentException("Cannot add two unlimited positional arguments");
            }
            allowUnlimitedPositionalArgument = false;
        }
    }

    private void throwIfArgumentAlreadyAdded(PositionalArgument<?> argument) {
        if (isAlreadyAdded(argument)) {
            throw new IllegalArgumentException(String.format("Argument %s already added", argument.getName()));
        }
    }

    private boolean isAlreadyAdded(PositionalArgument<?> argument) {
        return positionalArguments.stream()
                .map(data -> data.argument)
                .anyMatch(arg -> arg.equals(argument));
    }

    public void parse(String[] args) {
        int unlimitedPositionalArgumentPos = getUnlimitedPositionalArgumentPos();
        if (unlimitedPositionalArgumentPos == -1 && args.length > positionalArguments.size()) {
            int pos = positionalArguments.size() + 1;
            throw new ArgumentParserException(
                    String.format("Unexpected %s argument", pos + getSuffix(pos))
            );
        }

        int end = unlimitedPositionalArgumentPos == -1 ? positionalArguments.size() : unlimitedPositionalArgumentPos;
        for (int i = 0; i < end; i++) {
            PositionalArgumentData positionalArgumentData = positionalArguments.get(i);
            if (i >= args.length && positionalArgumentData.required) {
                throw new ArgumentParserException(
                        String.format(
                                "Missing %s positional argument (%s)",
                                (i + 1) + getSuffix(i + 1),
                                positionalArgumentData.argument.getName()
                        )
                );
            }

            if (i >= args.length) {
                return;
            }

            positionalArgumentData.argument.parse(args[i]);
        }

        if (unlimitedPositionalArgumentPos == -1) {
            return;
        }

        PositionalArgumentData unlimitedPositionalArgumentData = positionalArguments.get(unlimitedPositionalArgumentPos);
        int i2 = args.length;
        for (int i = positionalArguments.size() - 1; i > unlimitedPositionalArgumentPos; i--) {
            int i1 = positionalArguments.size() - i;
            i2 = args.length - i1;
            if ((i2 == end && unlimitedPositionalArgumentData.required) || i2 < end) {
                throwMissingPositionalArgument(end + i1);
            }
            PositionalArgumentData positionalArgumentData = positionalArguments.get(i);
            positionalArgumentData.argument.parse(args[i2]);
        }
        String[] unlimitedPosArgValues = Arrays.copyOfRange(args, end, i2);
        if (unlimitedPosArgValues.length == 0 && unlimitedPositionalArgumentData.required) {
            throwMissingPositionalArgument(end);
        }
        ((UnlimitedPositionalArgument<?>)unlimitedPositionalArgumentData.argument).parse(unlimitedPosArgValues);
    }

    private void throwMissingPositionalArgument(int index) {
        PositionalArgumentData positionalArgumentData = positionalArguments.get(index);
        throw new ArgumentParserException(
                String.format(
                        "Missing %s argument (%s)",
                        index + 1 + getSuffix(index + 1),
                        positionalArgumentData.argument.getName()
                )
        );
    }

    private int getUnlimitedPositionalArgumentPos() {
        for (int i = 0; i < positionalArguments.size(); i++) {
            PositionalArgumentData argData = positionalArguments.get(i);
            if (argData.argument instanceof UnlimitedPositionalArgument<?>) {
                return i;
            }
        }
        return -1;
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

    private static class PositionalArgumentData {

        private final PositionalArgument<?> argument;
        private final boolean required;

        public PositionalArgumentData(PositionalArgument<?> argument, boolean required) {
            this.argument = argument;
            this.required = required;
        }
    }
}
