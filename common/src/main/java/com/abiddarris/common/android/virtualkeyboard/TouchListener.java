package com.abiddarris.common.android.virtualkeyboard;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class TouchListener implements OnTouchListener {
    
    private int keycode;
    private VirtualKeyboard keyboard;
    
    TouchListener(VirtualKeyboard keyboard, int keycode) {
        this.keyboard = keyboard;
        this.keycode = keycode;
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                keyboard.sendKeyDownEvent(keycode);
                return true;    
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                keyboard.sendKeyUpEvent(keycode);
                return true;
            default :
                return false;        
        }
    }
    
    
}
