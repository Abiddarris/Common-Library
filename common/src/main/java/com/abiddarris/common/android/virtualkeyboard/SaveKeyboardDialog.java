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

import android.os.Bundle;

import com.abiddarris.common.R;
import com.abiddarris.common.android.dialogs.EditTextDialog;
import com.abiddarris.common.android.utils.TextListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SaveKeyboardDialog extends EditTextDialog {

    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
        
        var ui = getUI();
        
        getUI().textInputLayout.setHint(R.string.name);
        getUI().textInputLayout.setSuffixText(getString(R.string.json_extension));
        ui.textInputLayout.getEditText()
            .addTextChangedListener(TextListener.newTextListener(editable -> {
                String text = editable.toString();
                int errorMessageRes = -1;    
                if(text.isBlank()) {
                    errorMessageRes = R.string.name_blank_error;
                }
                ui.textInputLayout.setError(errorMessageRes == -1 ? "" : getString(errorMessageRes));
                ui.textInputLayout.setErrorEnabled(errorMessageRes != -1); 
            }));
        
        builder.setTitle(R.string.save)
            .setPositiveButton(android.R.string.ok, null);
    }
}
