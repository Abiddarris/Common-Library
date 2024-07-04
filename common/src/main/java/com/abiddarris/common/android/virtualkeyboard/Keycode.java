/***********************************************************************************
 * Copyright 2024 Abiddarris
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
package com.abiddarris.common.android.virtualkeyboard;

import static android.view.KeyEvent.*;

public enum Keycode {
    KEY_ARROW_UP(KEYCODE_DPAD_UP),
    KEY_ARROW_DOWN(KEYCODE_DPAD_DOWN),
    KEY_ARROW_LEFT(KEYCODE_DPAD_LEFT),
    KEY_ARROW_RIGHT(KEYCODE_DPAD_RIGHT),
    KEY_A(KEYCODE_A),
    KEY_B(KEYCODE_B),
    KEY_C(KEYCODE_C),
    KEY_D(KEYCODE_D),
    KEY_E(KEYCODE_E),
    KEY_F(KEYCODE_F),
    KEY_G(KEYCODE_G),
    KEY_H(KEYCODE_H),
    KEY_I(KEYCODE_I),
    KEY_J(KEYCODE_J),
    KEY_K(KEYCODE_K),
    KEY_L(KEYCODE_L),
    KEY_M(KEYCODE_M),
    KEY_N(KEYCODE_N),
    KEY_O(KEYCODE_O),
    KEY_P(KEYCODE_P),
    KEY_Q(KEYCODE_Q),
    KEY_R(KEYCODE_R),
    KEY_S(KEYCODE_S),
    KEY_T(KEYCODE_T),
    KEY_U(KEYCODE_U),
    KEY_V(KEYCODE_V),
    KEY_W(KEYCODE_W),
    KEY_X(KEYCODE_X),
    KEY_Y(KEYCODE_Y),
    KEY_Z(KEYCODE_Z),
    KEY_SPACE(KEYCODE_SPACE),
    KEY_ENTER(KEYCODE_ENTER),
    KEY_SHIFT_LEFT(KEYCODE_SHIFT_LEFT),
    KEY_SHIFT_RIGHT(KEYCODE_SHIFT_RIGHT),
    KEY_ALT_LEFT(KEYCODE_ALT_LEFT),
    KEY_ALT_RIGHT(KEYCODE_ALT_RIGHT),
    KEY_CAPS_LOCK(KEYCODE_CAPS_LOCK),
    KEY_CTRL_LEFT(KEYCODE_CTRL_LEFT),
    KEY_CTRL_RIGHT(KEYCODE_CTRL_RIGHT),
    KEY_TAB(KEYCODE_TAB),
    KEY_ESCAPE(KEYCODE_ESCAPE),
    KEY_BACKSPACE(KEYCODE_DEL),
    KEY_0(KEYCODE_0),
    KEY_1(KEYCODE_1),
    KEY_2(KEYCODE_2),
    KEY_3(KEYCODE_3),
    KEY_4(KEYCODE_4),
    KEY_5(KEYCODE_5),
    KEY_6(KEYCODE_6),
    KEY_7(KEYCODE_7),
    KEY_8(KEYCODE_8),
    KEY_9(KEYCODE_9),
    KEY_COMMA(KEYCODE_COMMA),
    KEY_PERIOD(KEYCODE_PERIOD),
    KEY_BACKTICK(KEYCODE_GRAVE),
    KEY_MINUS(KEYCODE_MINUS),
    KEY_EQUALS(KEYCODE_EQUALS),
    KEY_LEFT_BRACKET(KEYCODE_LEFT_BRACKET),
    KEY_RIGHT_BRACKET(KEYCODE_RIGHT_BRACKET),
    KEY_BACKSLASH(KEYCODE_BACKSLASH),
    KEY_SEMICOLON(KEYCODE_SEMICOLON),
    KEY_APOSTROPHE(KEYCODE_APOSTROPHE),
    KEY_SLASH(KEYCODE_SLASH),
    KEY_F1(KEYCODE_F1),
    KEY_F2(KEYCODE_F2), 
    KEY_F3(KEYCODE_F3),
    KEY_F4(KEYCODE_F4),
    KEY_F5(KEYCODE_F5), 
    KEY_F6(KEYCODE_F6),
    KEY_F7(KEYCODE_F7),
    KEY_F8(KEYCODE_F8), 
    KEY_F9(KEYCODE_F9),
    KEY_F10(KEYCODE_F10),
    KEY_F11(KEYCODE_F11), 
    KEY_F12(KEYCODE_F12),
    KEY_INSERT(KEYCODE_INSERT),
    KEY_PAGE_UP(KEYCODE_PAGE_UP),
    KEY_DELETE(KEYCODE_FORWARD_DEL),
    KEY_HOME(KEYCODE_MOVE_HOME),
    KEY_END(KEYCODE_MOVE_END),
    KEY_PAGE_DOWN(KEYCODE_PAGE_DOWN);

    private int keycode;

    private Keycode(int keycode) {
        this.keycode = keycode;
    }

    public int getKeycode() {
        return keycode;
    }

    @Override
    public String toString() {
        return super.toString().substring(4);
    }
}
