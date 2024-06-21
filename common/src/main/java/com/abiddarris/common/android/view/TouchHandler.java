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
package com.abiddarris.common.android.view;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

class TouchHandler implements OnTouchListener {
   
    private float dX, dY;
    private MoveableViewsGroup group;
    private OnTouchListener listener;
    
    public TouchHandler(MoveableViewsGroup group, OnTouchListener listener) {
        this.group = group;
        this.listener = listener;
    }
    
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(group.isEdit()) {
            return handleMove(view, event);
        }
        
        return listener == null ? false : listener.onTouch(view, event);
    }
    
    private boolean handleMove(View view, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                view.setX(event.getRawX() + dX);
                view.setY(event.getRawY() + dY);
                view.bringToFront();
                view.invalidate();
            
                return true;
            default:
                return false;
        }
    }
}
