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
import android.widget.Button;
import android.widget.RelativeLayout;
import com.abiddarris.common.android.view.MoveableViewsGroup;

public class VirtualKeyboard extends MoveableViewsGroup {
    
    
    public VirtualKeyboard(Context context) {
        super(context);
    }
    
    protected void sendKeyDownEvent(int keycode) {
        
    }
    
    protected void sendKeyUpEvent(int keycode) {
        
    }
    
    public void addButton(String title, int keycode) {
        Button button = new Button(getContext());
        button.setText(title);
        
        addMoveableView(button, new LayoutParams(100, 100), new TouchListener(this, keycode));
    }

    
}
