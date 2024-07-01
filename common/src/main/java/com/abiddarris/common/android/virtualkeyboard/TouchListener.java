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

import static com.abiddarris.common.android.handlers.MainThreads.postDelayed;
import static com.abiddarris.common.android.handlers.MainThreads.removeCallbacks;
import static com.abiddarris.common.android.virtualkeyboard.Event.DOWN;
import static com.abiddarris.common.android.virtualkeyboard.Event.UP;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class TouchListener implements Runnable, OnTouchListener {
    
    private static final int INITIAL_DELAY = 500;
    private static final int INTERVAL = 50;
    
    private Key key;
    private Runnable initialKeyDispatcher = () -> postDelayed(this, INTERVAL);
    private VirtualKeyboard keyboard;
    
    TouchListener(VirtualKeyboard keyboard, Key key) {
        this.keyboard = keyboard;
        this.key = key;
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(key.getKeycode() == null) {
            return false;
        }
        
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                postDelayed(this, INITIAL_DELAY);
                
                sendKeyEvent(DOWN);
                return false;    
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                removeCallbacks(initialKeyDispatcher);
                removeCallbacks(this);
                
                sendKeyEvent(UP);
                return false;
            default :
                return false;        
        }
    }
    
    @Override
    public void run() {
        sendKeyEvent(DOWN);
        postDelayed(this, INTERVAL);
    }
    
    private void sendKeyEvent(Event event) {
    	Keycode code = key.getKeycode();
        if(code == null) {
            return;
        }
        
        keyboard.sendKeyEvent(event, code.getKeycode());
    }
    
}
