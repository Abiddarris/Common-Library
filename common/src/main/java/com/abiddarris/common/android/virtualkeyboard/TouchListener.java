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
            return true;
        }
        
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                keyboard.sendKeyEvent(Event.DOWN, code.getKeycode());
                return true;    
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                keyboard.sendKeyEvent(Event.UP, code.getKeycode());
                return true;
            default :
                return false;        
        }
    }
    
    
}
