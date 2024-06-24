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

import static android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT;

import android.widget.Button;

public class Size {

    public static final int AUTO = 0;
    public static final int CUSTOM = 1;

    private boolean calculated;
    private int type;
    private int width, height;
    private Key key;

    Size(Key key) {
        this.key = key;
    }
    
    public int getType() {
        calculateIfNot();
        
        return type;
    }
    
    public int getWidth() {
        calculateIfNot();
        
        return width;
    }
    
    public int getHeight() {
    	calculateIfNot();
        
        return height;
    }
    
    public void calculate() {
        Button button = key.getButton();
        if(button == null) {
            return;
        }
        
        var layoutParams = button.getLayoutParams();
        if(layoutParams.width == WRAP_CONTENT && layoutParams.height == WRAP_CONTENT) {
            type = AUTO;
        } else {
            type = CUSTOM;
        }
        
        width = button.getWidth();
        height = button.getHeight();
        
        calculated = true;
    }
    
    private void calculateIfNot() {
        if(!calculated)
            calculate();
    }
}