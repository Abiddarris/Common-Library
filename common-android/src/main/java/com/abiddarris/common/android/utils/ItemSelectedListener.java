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
package com.abiddarris.common.android.utils;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import java.util.Objects;

public class ItemSelectedListener implements OnItemSelectedListener {
    
    private OnItemSelectedListener listener;
    
    public ItemSelectedListener(SimpleItemSelectedListener listener) {
    	Objects.requireNonNull(listener);
        
        this.listener = (type, adapter, view, index, id) -> {
            switch(type) {
                case ITEM :
                    listener.onItemSelected(index);
                    break;
            }
        };
    }
    
    public ItemSelectedListener(OnItemSelectedListener listener) {
    	Objects.requireNonNull(listener);
        
        this.listener = listener;
    }
    
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int index, long id) {
        listener.onSelected(Type.ITEM, adapterView, view, index, id);
    }
    
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        listener.onSelected(Type.NOTHING, adapterView, null, -1, -1);
    }
    
    public static enum Type {
        NOTHING, ITEM
    }
    
    public static interface OnItemSelectedListener {
        void onSelected(Type type, AdapterView<?> adapterView, View view, int index, long id);
    }
    
    public static interface SimpleItemSelectedListener {
        void onItemSelected(int index);
    }
    
}
