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
package com.abiddarris.common.android.dialogs;

import android.content.Context;
import android.view.View;

/**
 * Class that provide default view and prevent {@code setView} 
 * from being called
 */
public class DefaultViewDialogBuilder extends DialogBuilder {
    
    public DefaultViewDialogBuilder(Context context, View view) {
        super(context);
        
        super.setView(view);
    }
    
    @Override
    public DialogBuilder setView(View view) {
        throw new UnsupportedOperationException("Cannot set custom view on SingleChoiceDialog");
    }
}
