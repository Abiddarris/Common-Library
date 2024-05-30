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

import android.app.Dialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.DialogFragment;
import com.abiddarris.common.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BaseDialogFragment extends DialogFragment {
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        MaterialAlertDialogBuilder builder = createDialog();
        View view = createView();
        if(view != null) {
            Resources resources = getResources();
            int padding = (int)resources.getDimension(R.dimen.abc_dialog_padding_material);
            int paddingTop = (int)resources.getDimension(R.dimen.abc_dialog_padding_top_material);
            view.setPadding(view.getPaddingLeft() + padding, 
                view.getPaddingRight() + padding, 
                view.getPaddingTop() + paddingTop,
                view.getPaddingBottom() + (hasButton() ? 0 : (int)resources.getDimension(R.dimen.abc_dialog_list_padding_bottom_no_buttons)));
            
            builder.setView(view);
        }
        
        return builder.create();
    }
    
    protected MaterialAlertDialogBuilder createDialog() {
    	return new MaterialAlertDialogBuilder(getContext());
    }
    
    protected View createView() {
        return null;
    }
    
    protected boolean hasButton() {
        return false;
    }
}
