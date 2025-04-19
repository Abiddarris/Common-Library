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
package com.abiddarris.terminal.parser;

import com.abiddarris.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultParser implements Parser {

    @Override
    public Action parse(Terminal terminal, String command) {
        EscapedCharSequence sequence = new EscapedCharSequence(command);
        sequence.strip();

        new VariableExpander(terminal, sequence);

        removeDuplicateSpace(sequence);

        List<EscapedCharSequence> args = splitBySpace(sequence);
        return Objects.requireNonNullElseGet(
                checkForAssignment(args), () -> new ExecuteCommandAction(turnIntoString(args)));

    }

    private static VariableAssignmentAction checkForAssignment(List<EscapedCharSequence> args) {
        if (args.isEmpty()) {
            return null;
        }

        EscapedCharSequence seq = args.get(0);
        int c = seq.findChar('=');
        if (c < 0 || c == seq.length() - 1 || seq.insideQuote(c)) {
            return null;
        }

        EscapedCharSequence name = seq.subSequence(0, c);
        if (!VariableExpander.checkVariableNameValid(name, false)) {
            return null;
        }

        EscapedCharSequence value = seq.subSequence(c + 1, seq.length());

        return new VariableAssignmentAction(name.toString(false), value.toString(false));
    }

    private String[] turnIntoString(List<EscapedCharSequence> args) {
        return args.stream()
                .map(seq -> {
                    if (seq.isEmpty()) {
                        return seq;
                    }

                    char firstChar = seq.charAt(0);
                    if ((firstChar == '"' || firstChar == '\'') && !seq.isEscaped(0)) {
                        seq.deleteChar(0);
                    }

                    int i = seq.length() - 1;
                    if (i < 0) {
                        return seq;
                    }

                    char lastChar = seq.charAt(i);
                    if ((lastChar == '"' || lastChar == '\'') && !seq.isEscaped(i)) {
                        seq.deleteChar(i);
                    }

                    return seq;
                })
                .map(seq -> seq.toString(false))
                .toArray(String[]::new);
    }

    private void removeDuplicateSpace(EscapedCharSequence sequence) {
        boolean dropUpcomingSpace = false;
        for (int i = 0; i < sequence.length(); i++) {
            if (!(sequence.actualChar(i, ' ') && !sequence.insideQuote(i))) {
                dropUpcomingSpace = false;
                continue;
            }

            if (dropUpcomingSpace) {
                sequence.deleteChar(i);
                i--;
                continue;
            }
            dropUpcomingSpace = true;
        }
    }

    private List<EscapedCharSequence> splitBySpace(EscapedCharSequence sequence) {
        List<EscapedCharSequence> args = new ArrayList<>();
        int lastSpace = -1;
        for (int i = 0; i < sequence.length(); i++) {
            if (sequence.actualChar(i, ' ') && !sequence.insideQuote(i)) {
                args.add(sequence.subSequence(lastSpace + 1, i));
                lastSpace = i;
            }
        }
        args.add(sequence.subSequence(lastSpace + 1, sequence.length()));
        return args;
    }

    static class EscapedCharSequence implements CharSequence {

        private final List<Char> chars = new ArrayList<>();

        public EscapedCharSequence(String str) {
            for (int i = 0; i < str.length();) {
                char c = str.charAt(i);
                i++;
                if (c != '\\') {
                    addChar(c, false);
                    continue;
                }

                if (i >= str.length()) {
                    throw new IllegalArgumentException("Expected a character after \\");
                }

                c = str.charAt(i);
                i++;
                addChar(c, true);
            }
        }

        private EscapedCharSequence(List<Char> chars) {
            this.chars.addAll(chars);
        }

        private void strip() {
            for (int i = 0; i < length(); i++) {
                if (actualChar(i, ' ')) {
                    deleteChar(i--);
                    continue;
                }
                break;
            }

            for (int i = length() - 1; i >= 0; i--) {
                if (actualChar(i, ' ')) {
                    deleteChar(i);
                    continue;
                }
                break;
            }
        }

        public void addChar(char c, boolean escaped) {
            insertChar(chars.size(), c, escaped);
        }

        public void deleteChar(int i) {
            this.chars.remove(i);
        }

        @Override
        public int length() {
            return chars.size();
        }

        @Override
        public char charAt(int i) {
            return chars.get(i).c;
        }

        public boolean isEscaped(int i) {
            return chars.get(i).escaped;
        }

        public boolean insideQuote(int i) {
            return getQuoteType(i) != QuoteType.NONE;
        }

        public boolean isEmpty() {
            return length() == 0;
        }
        
        public QuoteType getQuoteType(int i) {
            QuoteType type = QuoteType.NONE;
            for (int j = 0; j < chars.size(); j++) {
                if (actualChar(j, '"')) {
                    if (type == QuoteType.DOUBLE) {
                        type = QuoteType.NONE;
                        continue;
                    }

                    type = QuoteType.DOUBLE;
                    if (j == i) {
                        return QuoteType.NONE;
                    }
                } else if (actualChar(j, '\'')) {
                    if (type == QuoteType.QUOTE) {
                        type = QuoteType.NONE;
                        continue;
                    }

                    type = QuoteType.QUOTE;
                    if (j == i) {
                        return QuoteType.NONE;
                    }
                }

                if (j == i) {
                    return type;
                }
            }
            return QuoteType.NONE;
        }

        public boolean actualChar(int i, char c) {
            return charAt(i) == c && !isEscaped(i);
        }

        @Override
        public EscapedCharSequence subSequence(int i, int i1) {
            return new EscapedCharSequence(chars.subList(i, i1));
        }

        @Override
        public String toString() {
            return toString(true);
        }

        public String toString(boolean includeEscape) {
            StringBuilder builder = new StringBuilder(chars.size() * (includeEscape ? 2 : 1));
            for (Char c : chars) {
                if (c.escaped && includeEscape) {
                    builder.append("\\");
                }
                builder.append(c.c);
            }
            return builder.toString();
        }

        public void delete(int start, int end) {
            for (int i = 0; i < end - start; i++) {
                deleteChar(start);
            }
        }

        public void insertRaw(int i, String value) {
            for (char c : value.toCharArray()) {
                insertChar(i++, c, false);
            }
        }

        public void insertChar(int i, char c, boolean escaped) {
            this.chars.add(i, new Char(c, escaped));
        }

        public int findChar(char c) {
            for (int i = 0; i < length(); i++) {
                if (actualChar(i, c)) {
                    return i;
                }
            }
            return -1;
        }

        private static class Char {
            private final char c;
            private final boolean escaped;

            public Char(char c, boolean escaped) {
                this.c = c;
                this.escaped = escaped;
            }
        }

    }

    public enum QuoteType {
        NONE, QUOTE, DOUBLE
    }
}
