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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.abiddarris.common.android.R;

public class KeySpinner extends ArrayAdapter<Keycode> {

    public KeySpinner(Context context) {
        super(context, R.layout.layout_item, Keycode.values());
    }

    @Override
    public View getDropDownView(int pos, View view, ViewGroup group) {
        return getViewInternal(pos, view, group);
    }

    @Override
    public View getView(int pos, View view, ViewGroup group) {
        return getViewInternal(pos, view, group);
    }
    
    private View getViewInternal(int pos, View view, ViewGroup group) {
        if(view == null) {
            view = LayoutInflater.from(getContext())
                .inflate(R.layout.layout_item, group, false);
        }
        
        TextView textView = (TextView)view;
        textView.setText(getItem(pos).toString());
        
        return view;
    }
}
