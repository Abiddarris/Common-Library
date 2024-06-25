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
import android.widget.LinearLayout;
import com.abiddarris.common.R;
import com.abiddarris.common.databinding.LayoutVirtualKeyboardOptionsBinding;

public class VirtualKeyboardOptions extends LinearLayout {
    
    private LayoutVirtualKeyboardOptionsBinding binding;
    
    public VirtualKeyboardOptions(Context context, VirtualKeyboard keyboard) {
        super(context);
        
        binding = LayoutVirtualKeyboardOptionsBinding.inflate(
            LayoutInflater.from(context), this);
        binding.edit.setOnClickListener(v -> {
            keyboard.setEdit(!keyboard.isEdit());
             
            int visibility = keyboard.isEdit() ? VISIBLE : GONE;
            
            binding.add.setVisibility(visibility); 
            binding.setting.setVisibility(visibility);     
        });
        
        binding.add.setOnClickListener(v -> keyboard.addButton());
        
        keyboard.addView(this);
    }
    
}
