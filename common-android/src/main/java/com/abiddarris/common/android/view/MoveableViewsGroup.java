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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class MoveableViewsGroup extends RelativeLayout {
    
    private boolean edit;
    private float dX, dY;
    
    public MoveableViewsGroup(Context context) {
        super(context);
    }
    
    public MoveableViewsGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoveableViewsGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    protected boolean onChildTouch(View view, MotionEvent event) {
        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                view.setX(
                    validatePos(getWidth() - view.getWidth(), event.getRawX() + dX));
                view.setY(
                    validatePos(getHeight() - view.getHeight(), event.getRawY() + dY));
                view.bringToFront();
                view.invalidate();
            
                return true;
            default:
                return false;
        }
    }
    
    private float validatePos(int bound, float pos) {
        return pos <= 0 ? 0 : (pos >= bound ? bound : pos);
    }
    
    public void addMoveableView(View view, LayoutParams params, OnTouchListener listener) {
        view.setOnTouchListener(new TouchHandler(this, listener));
        
        addView(view, params);
    }
    
    public boolean isEdit() {
        return this.edit;
    }
    
    public void setEdit(boolean edit) {
        this.edit = edit;
    }
    
}
