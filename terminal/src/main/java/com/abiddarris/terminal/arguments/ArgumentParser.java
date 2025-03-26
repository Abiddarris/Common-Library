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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class ArgumentParser {

    private final List<PositionalArgumentData> positionalArguments = new ArrayList<>();
    private final List<OptionArgumentData> options = new ArrayList<>();
    private boolean allowRequiredForPositionalArgument = true;
    private boolean allowUnlimitedPositionalArgument = true;

    public void require(PositionalArgument<?> argument) {
        validateArgument(argument);

        if (!allowRequiredForPositionalArgument) {
            throw new IllegalStateException("Cannot add required positional argument after optional positional argument");
        }
        checkItIsUnlimitedPositionalArgument(argument);

        if (argument.getValue() != null) {
            argument.setValue(null);
        }

        positionalArguments.add(new PositionalArgumentData(argument, true));
    }

    public void optional(PositionalArgument<?> argument) {
        validateArgument(argument);
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

    private void validateArgument(Argument<?> argument) {
        checkNonNull(argument, "Cannot be null");
        checkArgumentNotAlreadyAdded(argument);
        checkNameAlreadyExists(argument);
    }

    private void checkArgumentNotAlreadyAdded(Argument<?> argument) {
        if (argument instanceof PositionalArgument<?>) {
            if (positionalArguments.stream()
                    .map(data -> data.argument)
                    .anyMatch(arg -> arg.equals(argument))) {
                throw new IllegalArgumentException(String.format("Argument %s already added", argument.getName()));
            }
            return;
        }

        if (argument instanceof BaseOption<?>) {
            if (options.stream()
                    .map(data -> data.argument)
                    .anyMatch(opt -> opt.equals(argument))) {
                throw new IllegalArgumentException(String.format("Option %s already added", argument.getName()));
            }
            return;
        }

        throw new IllegalArgumentException("Unknown argument type : " + argument.getClass());
    }

    private void checkNameAlreadyExists(Argument<?> argument) {
        if (positionalArguments.stream()
                .map(data -> data.argument.getName())
                .anyMatch(name -> name.equals(argument.getName()))) {
            throw new IllegalArgumentException(String.format("Argument with name %s already exists", argument.getName()));
        }
    }

    public void parse(String[] args) {
        args = parseOption(args);
        parsePositionalArguments(args);
    }

    private String[] parseOption(String[] args) {
        int lastOption = getLastOptionIndex(args);
        if (lastOption == -1) {
            return args;
        }

        int end = Math.min(args.length, lastOption + 2);

        String[] optArgs = Arrays.copyOfRange(args, 0, end);
        Map<String, List<String>> options = mapValuesToKey(optArgs);
        mapShortNameToLongName(options);
        mapValuesToOptionObject(options);

        for (String option : options.keySet()) {
            throw new ArgumentParserException("Unknown option : " + option.substring(2));
        }

        return Arrays.copyOfRange(args, end, args.length);
    }

    private void mapValuesToOptionObject(Map<String, List<String>> options) {
        for (OptionArgumentData data : this.options) {
            List<String> values = options.remove("--" + data.argument.getName());
            if (values == null) {
                continue;
            }

            if (values.size() > 1) {
                throw new ArgumentParserException(
                        data.argument.getName() + " only requires one value");
            }

            if (values.isEmpty()) {
                throw new ArgumentParserException("Missing value for " + data.argument.getName());
            }

            data.argument.parse(values.get(0));
        }
    }

    private void mapShortNameToLongName(Map<String, List<String>> options) {
        for (String key : new HashSet<>(options.keySet())) {
            char shortName = key.charAt(1);
            if (key.length() != 2 || !key.startsWith("-") || shortName == '-') {
                continue;
            }

            Option<?> option = getOptionFromShort(shortName);
            if (option == null) {
                throw new ArgumentParserException("Unknown option : " + shortName);
            }

            List<String> values = options.computeIfAbsent("--" + option.getName(), (str) -> new ArrayList<>());
            values.addAll(options.get(key));

            options.remove(key);
        }
    }

    private static Map<String, List<String>> mapValuesToKey(String[] optArgs) {
        Map<String, List<String>> options = new HashMap<>();
        List<String> values = null;
        for (String optArg : optArgs) {
            if (optArg.startsWith("-")) {
                values = options.computeIfAbsent(optArg, (str) -> new ArrayList<>());
            } else if (values != null) {
                values.add(optArg);
            } else {
                throw new ArgumentParserException("Unknown value : " + optArg);
            }
        }
        return options;
    }

    private static int getLastOptionIndex(String[] args) {
        int lastOption = -1;
        for (int i = args.length - 1; i >= 0; i--) {
            if (args[i].startsWith("-")) {
                lastOption = i;
                break;
            }
        }
        return lastOption;
    }

    private Option<?> getOptionFromShort(char c) {
        for (OptionArgumentData data : options) {
            for (char shortName : data.argument.getShortNames()) {
                if (c == shortName) {
                    return data.argument;
                }
            }
        }
        return null;
    }

    private void parsePositionalArguments(String[] args) {
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

    public void optional(Option<?> option) {
        validateArgument(option);
        checkNoSameShortNames(option);

        options.add(new OptionArgumentData(option, false));
    }

    private void checkNoSameShortNames(Option<?> option) {
        for (char shortName : option.getShortNames()) {
            if (getOptionFromShort(shortName) != null) {
                throw new IllegalArgumentException(String.format(
                        "Multiple options with same short name (%s) detected", shortName
                ));
            }
        }
    }

    private static class PositionalArgumentData {

        private final PositionalArgument<?> argument;
        private final boolean required;

        public PositionalArgumentData(PositionalArgument<?> argument, boolean required) {
            this.argument = argument;
            this.required = required;
        }
    }

    private static class OptionArgumentData {

        private final Option<?> argument;
        private final boolean required;

        public OptionArgumentData(Option<?> argument, boolean required) {
            this.argument = argument;
            this.required = required;
        }
    }
}
