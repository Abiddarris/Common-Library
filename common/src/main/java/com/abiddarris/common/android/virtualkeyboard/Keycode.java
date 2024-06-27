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
    KEY_SPACE(KEYCODE_SPACE);
    
    private int keycode;
    
    private Keycode(int keycode) {
        this.keycode = keycode;
    }
    
    public int getKeycode() {
        return keycode;
    }
}
