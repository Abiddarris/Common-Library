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

import static com.abiddarris.common.android.utils.ScreenUtils.dpToPixel;
import static com.abiddarris.common.android.utils.ScreenUtils.pixelToDp;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.HEIGHT;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.TYPE;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.WIDTH;

import static java.lang.Math.abs;

import android.content.Context;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import org.json.JSONException;
import org.json.JSONObject;

public class Size {

    public static final int AUTO = 0;
    public static final int CUSTOM = 1;

    private boolean calculated;
    private int type;
    private float width, height;
    private Key key;

    Size(Key key) {
        this.key = key;
    }
    
    JSONObject save() throws JSONException {
        calculate();
        
    	JSONObject size = new JSONObject();
        size.put(TYPE, getType());
        
        if(type == CUSTOM) {
            size.put(WIDTH, getWidth());
            size.put(HEIGHT, getHeight());
        }
        
        return size;
    }
    
    void load(JSONObject size) throws JSONException {
        setType(size.getInt(TYPE));
        
        if(type != CUSTOM) {
            return;
        }
        
        float width = (float)size.getDouble(WIDTH);
        float height = (float)size.getDouble(HEIGHT);
        
        setSize(width, height);
    }
    
    public int getType() {
        calculateIfNot();
        
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
        
        if(type == AUTO) {
            updateSizeInternal(WRAP_CONTENT, WRAP_CONTENT);
        }
    }
    
    public void setSize(float width, float height) {
        if(type != CUSTOM) {
            throw new IllegalStateException("Cannot set size if size type is not CUSTOM");
        }
        
        width = abs(width);
        height = abs(height);
        
        Button button = key.getButton();
        Context context = button.getContext();
        
        updateSizeInternal(
            Math.round(dpToPixel(context, width)),
            Math.round(dpToPixel(context, height))
        );
        
        this.width = width;
        this.height = height;
        
        calculated = true;
    }
    
    private void updateSizeInternal(int widthPx, int heightPx) {
        Button button = key.getButton();
        RelativeLayout parent = (RelativeLayout)button.getParent();
        LayoutParams params = (LayoutParams)button.getLayoutParams();
        params.width = widthPx;
        params.height = heightPx;
        
        parent.updateViewLayout(button, params);
    }
    
    public float getWidth() {
        calculateIfNot();
        
        return width;
    }
    
    public float getHeight() {
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
        
        Context context = button.getContext();
        
        width = pixelToDp(context, button.getWidth());
        height = pixelToDp(context, button.getHeight());
        
        calculated = true;
    }
    
    public boolean isCalculated() {
        return calculated;
    }
    
    private void calculateIfNot() {
        if(!calculated)
            calculate();
    }
}
