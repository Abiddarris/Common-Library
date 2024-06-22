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

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.abiddarris.common.R;
import com.abiddarris.common.android.view.MoveableViewsGroup;
import java.util.Random;

public class VirtualKeyboard extends MoveableViewsGroup {
    
    private Button editButton;
    private KeyListener listener;
    
    public VirtualKeyboard(Context context) {
        super(context);
        
        editButton = new Button(getContext());
        editButton.setVisibility(GONE);
        editButton.setText(R.string.edit);
        
        addView(editButton);
    }
    
    @Override
    protected boolean onChildTouch(View view, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                editButton.setVisibility(GONE);
                break;
            case MotionEvent.ACTION_UP :
                editButton.setVisibility(VISIBLE);
                editButton.setX(view.getX() + 
                                view.getWidth() / 2 -
                                editButton.getWidth() / 2);
                editButton.setY(view.getY() + view.getHeight());
                editButton.bringToFront();
        }
        return super.onChildTouch(view, event);
    }
    
    protected void sendKeyEvent(Event event, int keycode) {
        var listener = getKeyListener();
        if(listener != null) {
            listener.onKey(event, keycode);
        }
    }
    
    public void addButton(String title, int keycode) {
        Button button = new Button(getContext());
        button.setText(title);
        
        addMoveableView(button, new LayoutParams(100, 100), new TouchListener(this, keycode));
    }
    
    public KeyListener getKeyListener() {
        return this.listener;
    }
    
    public void setKeyListener(KeyListener listener) {
        this.listener = listener;
    }
}
