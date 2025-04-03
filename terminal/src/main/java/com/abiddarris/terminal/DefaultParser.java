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
package com.abiddarris.terminal;

import java.util.ArrayList;
import java.util.List;

public class DefaultParser implements Parser {

    @Override
    public String[] parse(String command) {
        EscapedCharSequence sequence = new EscapedCharSequence(command);
        List<EscapedCharSequence> args = new ArrayList<>();
        int lastSpace = -1;
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            if (c == ' ' && !sequence.isEscaped(i)) {
                args.add(sequence.subSequence(lastSpace + 1, i));
                lastSpace = i;
            }
        }
        args.add(sequence.subSequence(lastSpace + 1, sequence.length()));

        return args.stream()
                .map(seq -> seq.toString(false))
                .toArray(String[]::new);
    }

    private static class EscapedCharSequence implements CharSequence {

        private final List<Char> chars = new ArrayList<>();

        public EscapedCharSequence(String str) {
            for (int i = 0; i < str.length();) {
                char c = str.charAt(i);
                i++;
                if (c != '\\') {
                    chars.add(new Char(c, false));
                    continue;
                }

                if (i >= str.length()) {
                    throw new IllegalArgumentException("Expected a character after \\");
                }

                c = str.charAt(i);
                i++;
                chars.add(new Char(c, true));
            }
        }

        private EscapedCharSequence(List<Char> chars) {
            this.chars.addAll(chars);
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

        private static class Char {
            private final char c;
            private final boolean escaped;

            public Char(char c, boolean escaped) {
                this.c = c;
                this.escaped = escaped;
            }
        }
    }
}
