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

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Simple interface for listening to EditText
 *
 * @since 1.0
 * @author Abiddarris
 */
public interface TextListener {
    
    /**
     * Called after text on {@code EditText} changed
     *
     * @param text {@code TextView}'s {@code Editable}
     * @since 1.0
     */
    void onTextChanged(Editable text);
    
    /**
     * Create new {@code TextWatcher} from given {@code TextListener}
     *
     * @param listener Callback
     * @return new {@code TextWatcher} 
     * @since 1.0
     */
    public static TextWatcher newTextListener(TextListener listener) {
        class TextWatcherImpl implements TextWatcher {
    
            @Override
            public void afterTextChanged(Editable editable) {
                listener.onTextChanged(editable);
            }
    
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
    
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }
        }
        return new TextWatcherImpl();
    }
   
}
