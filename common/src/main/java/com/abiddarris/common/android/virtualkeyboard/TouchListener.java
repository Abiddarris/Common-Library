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

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class TouchListener implements OnTouchListener {
    
    private Key key;
    private VirtualKeyboard keyboard;
    
    TouchListener(VirtualKeyboard keyboard, Key key) {
        this.keyboard = keyboard;
        this.key = key;
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        Keycode code = key.getKeycode();
        if(code == null) {
            return false;
        }
        
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                keyboard.sendKeyEvent(Event.DOWN, code.getKeycode());
                return false;    
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                keyboard.sendKeyEvent(Event.UP, code.getKeycode());
                return false;
            default :
                return false;        
        }
    }
    
    
}
