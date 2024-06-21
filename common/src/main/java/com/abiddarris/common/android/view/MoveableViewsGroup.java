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
import android.view.View;
import android.widget.RelativeLayout;

public class MoveableViewsGroup extends RelativeLayout {
    
    private boolean edit;
    
    public MoveableViewsGroup(Context context) {
        super(context);
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
