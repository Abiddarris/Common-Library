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
import android.content.res.Resources;
import android.view.View;
import com.abiddarris.common.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DialogBuilder extends MaterialAlertDialogBuilder {

    public DialogBuilder(Context context) {
        super(context);
    }
    
    public DialogBuilder setView(View view) {
        Resources resources = getContext().getResources();
        
        int padding = (int)resources.getDimension(R.dimen.abc_dialog_padding_material);
        int paddingTop = (int)resources.getDimension(R.dimen.abc_dialog_padding_top_material);
        int paddingBottom = (int)resources.getDimension(R.dimen.abc_dialog_list_padding_bottom_no_buttons);
        
        setView(view, padding, padding, paddingTop, paddingBottom);
            
        return this;    
    }
}
