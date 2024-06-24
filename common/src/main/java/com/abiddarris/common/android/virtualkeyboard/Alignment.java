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

import android.widget.Button;
import android.widget.RelativeLayout;

public class Alignment {

    public static final int TOP = 0;
    public static final int BOTTOM = 1;
    public static final int LEFT = 0;
    public static final int RIGHT = 2;

    private boolean calculated;
    private float marginX, marginY;
    private int flags;
    private Key key;

    Alignment(Key key) {
        this.key = key;
    }
    
    public int getFlags() {
        calculateIfNot();
        
        return this.flags;
    }
    
    public float getMarginX() {
        calculateIfNot();
        
        return this.marginX;
    }

    public float getMarginY() {
        calculateIfNot();
        
        return this.marginY;
    }

    public void calculate() {
        Button button = key.getButton();
        if (button == null) {
            return;
        }

        RelativeLayout parent = (RelativeLayout) button.getParent();
        float marginLeft = button.getX();
        float marginRight = parent.getWidth() - button.getX() - button.getWidth();

        if (marginLeft <= marginRight) {
            flags = Alignment.LEFT;
            marginX = marginLeft;
        } else {
            flags = Alignment.RIGHT;
            marginX = marginRight;
        }

        float marginTop = button.getY();
        float marginBottom = parent.getHeight() - button.getY() - button.getHeight();

        if (marginTop <= marginBottom) {
            flags |= Alignment.TOP;
            marginY = marginTop;
        } else {
            flags |= Alignment.BOTTOM;
            marginY = marginBottom;
        }
        
        calculated = true;
    }

    private void calculateIfNot() {
        if(!calculated)
            calculate();
    }
}
