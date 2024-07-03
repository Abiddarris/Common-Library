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

import static com.abiddarris.common.android.utils.ScreenUtils.dpToPixel;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.FLAGS;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.MARGIN_X;
import static com.abiddarris.common.android.virtualkeyboard.JSONKeys.MARGIN_Y;

import static java.lang.Math.max;

import android.content.Context;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.abiddarris.common.android.utils.ScreenUtils;
import com.abiddarris.common.android.view.listeners.AutoRemoveGlobalLayoutListener;

import org.json.JSONException;
import org.json.JSONObject;

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
    
    JSONObject save() throws JSONException {
        calculate();
        
        JSONObject alignment = new JSONObject();
        alignment.put(FLAGS, getFlags());
        alignment.put(MARGIN_X, getMarginX());
        alignment.put(MARGIN_Y, getMarginY());
        
        return alignment;
    }
    
    void load(JSONObject alignment) throws JSONException {
        int flags = alignment.getInt(FLAGS);
        float marginX = (float)alignment.getDouble(MARGIN_X);
        float marginY = (float)alignment.getDouble(MARGIN_Y);
       
        setMargins(flags, marginX, marginY);
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
    
    public void setMargins(int flags, float marginX, float marginY) {
        Button button = key.getButton();
        Context context = button.getContext();
        RelativeLayout parent = (RelativeLayout)button.getParent();
        Size size = key.getSize();
        
        marginX = max(0, marginX);
        marginY = max(0, marginY);
        
        if(!key.getSize().isCalculated()) {
            float marginXFinal = marginX, marginYFinal = marginY;
            new AutoRemoveGlobalLayoutListener(
                key.getButton().getViewTreeObserver(),
                 () -> setMargins(flags, marginXFinal, marginYFinal));
            return;
        }
        
        float marginXPixel = dpToPixel(context, marginX);
        float marginYPixel = dpToPixel(context, marginY);
        float x = 0, y = 0;
        
        switch(flags) {
            case LEFT | TOP :
                x = marginXPixel;
                y = marginYPixel;
                break;
            case RIGHT | TOP :
                x = parent.getWidth() - marginXPixel - dpToPixel(context, size.getWidth());
                y = marginYPixel;
                break;
            case LEFT | BOTTOM :
                x = marginXPixel;
                y = parent.getHeight() - marginYPixel - dpToPixel(context, size.getHeight());
                break;
            case RIGHT | BOTTOM :
                x = parent.getWidth() - marginXPixel - dpToPixel(context, size.getWidth());
                y = parent.getHeight() - marginYPixel - dpToPixel(context, size.getHeight());
        }
        
        button.setX(x);
        button.setY(y);
        
        this.flags = flags;
        this.marginX = marginX;
        this.marginY = marginY;
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
        
        Context context = button.getContext();
        
        marginX = ScreenUtils.pixelToDp(context, marginX);
        marginY = ScreenUtils.pixelToDp(context, marginY);
        
        calculated = true;
    }

    private void calculateIfNot() {
        if(!calculated)
            calculate();
    }
}
