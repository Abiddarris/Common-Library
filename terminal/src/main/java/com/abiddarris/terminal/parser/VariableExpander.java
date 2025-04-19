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

import com.abiddarris.terminal.ParseException;
import com.abiddarris.terminal.Terminal;

import java.util.ArrayList;
import java.util.List;

class VariableExpander {

    private static final List<Character> VALID_VARIABLE_CHARS = new ArrayList<>();
    private static final List<Character> NUMBER_CHARS = new ArrayList<>();

    static {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = upperCase.toLowerCase();
        String number = "0123456789";
        String all = upperCase + lowerCase + number + "_";

        populateList(all, VALID_VARIABLE_CHARS);
        populateList(number, NUMBER_CHARS);
    }

    private static void populateList(String s, List<Character> validVariableChars) {
        for (char c : s.toCharArray()) {
            validVariableChars.add(c);
        }
    }

    private final Terminal terminal;
    private final DefaultParser.EscapedCharSequence sequence;

    private int start = -1;
    private boolean acceptNumberOnly = false;
    private boolean requireClosingBracket = false;
    private int relativeIndex;
    private int i = 0;

    VariableExpander(Terminal terminal, DefaultParser.EscapedCharSequence sequence) {
        this.terminal = terminal;
        this.sequence = sequence;

        for (;i < sequence.length(); i++) {
            if (start == -1) {
                checkStartOfVariable(sequence, i);
                continue;
            }

            relativeIndex = i - start;
            char c = sequence.charAt(i);

            if (checkStartOfOpeningCurlyBrackets(sequence, c, i)) continue;
            if (checkEndOfCurlyBracket(c)) continue;
            if (handleNumber(sequence, c)) continue;

            if (!VALID_VARIABLE_CHARS.contains(c)) {
                if (relativeIndex == 1) {
                    unmark();
                    continue;
                }
                i = replaceVariableWithItsValue(start, i);
                unmark();
            }
        }

        if (start == -1 || start + 1 == sequence.length()) {
            return;
        }

        replaceVariableWithItsValue(start, sequence.length());
    }

    private boolean handleNumber(DefaultParser.EscapedCharSequence sequence, char c) {
        shouldMarkAsNumberOnly(c, 1);
        return checkAcceptNumberOnly(sequence, c);
    }

    private boolean checkAcceptNumberOnly(DefaultParser.EscapedCharSequence sequence, char c) {
        if (acceptNumberOnly) {
            if (NUMBER_CHARS.contains(c)) {
                return true;
            }

            sequence.delete(start, i);
            i = start - 1;
            unmark();
            return true;
        }
        return false;
    }

    private boolean checkEndOfCurlyBracket(char c) {
        if (requireClosingBracket) {
            shouldMarkAsNumberOnly(c, 2);
            if (c == '}') {
                i = replaceVariableWithItsValue(start, i + 1);
                unmark();

                requireClosingBracket = false;
                return true;
            }
            return true;
        }
        return false;
    }

    private void shouldMarkAsNumberOnly(char c, int index) {
        if (relativeIndex == index && NUMBER_CHARS.contains(c)) {
            acceptNumberOnly = true;
        }
    }

    private boolean checkStartOfOpeningCurlyBrackets(DefaultParser.EscapedCharSequence sequence, char c, int i) {
        if (c == '{') {
            if (sequence.isEscaped(i)) {
                unmark();
                return true;
            }

            if (requireClosingBracket) {
                throwBadSubstitution();
            }

            requireClosingBracket = true;
            return true;
        }
        return false;
    }

    private void throwBadSubstitution() {
        throw new ParseException("Bad substitution");
    }

    private void checkStartOfVariable(DefaultParser.EscapedCharSequence sequence, int i) {
        if (!sequence.actualChar(i, '$') ||
                sequence.getQuoteType(i) == DefaultParser.QuoteType.QUOTE) {
            return;
        }
        mark(i);
        acceptNumberOnly = false;
    }

    private void mark(int i) {
        start = i;
    }

    private void unmark() {
        start = -1;
    }

    private int replaceVariableWithItsValue(int start, int end) {
        DefaultParser.EscapedCharSequence varName = sequence.subSequence(start + 1, end);

        if (varName.charAt(0) == '{') {
            varName.deleteChar(0);
        }

        if (varName.charAt(varName.length() - 1) == '}') {
            varName.deleteChar(varName.length() - 1);
        }

        if (!checkVariableNameValid(varName, acceptNumberOnly)) {
            throwBadSubstitution();
        }

        String value = terminal.getVariable(varName.toString(false));
        value = value == null ? "" : value;

        sequence.delete(start, end);
        sequence.insertRaw(start, value);

        return start + value.length() - 1;
    }

    static boolean checkVariableNameValid(DefaultParser.EscapedCharSequence varName, boolean acceptNumberOnly) {
        String string = varName.toString(false);
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);

            if (acceptNumberOnly && !NUMBER_CHARS.contains(c)) {
                return false;
            }

            if (!acceptNumberOnly && i == 0 && NUMBER_CHARS.contains(c)) {
                return false;
            }

            if (!VALID_VARIABLE_CHARS.contains(c)) {
                return false;
            }
        }
        return true;
    }

}
