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
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.appcompat.app.AlertDialog;
import com.abiddarris.common.databinding.DialogEditTextBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Optional;

public class EditTextDialog extends BaseDialogFragment<String> {
   
    private static final String TEXT = "text";
    
    private DialogEditTextBinding ui;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new EditTextDialogBuilder(requireContext());
    }
    
    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);
        
        updateUI(getText());
    }
    
    public DialogEditTextBinding getUI() {
        return ui;
    }
    
    public void setText(String text) {
        saveVariable(TEXT, text);
        
        if(ui != null) {
            updateUI(text);
        }
    }
    
    public String getText() {
        return getVariable(TEXT);
    }
    
    private void updateUI(String text) {
        if(text == null) 
            text = "";
        if(!ui.textInputEditText.getText().toString().equals(text))
            ui.textInputEditText.setText(text);
    }
    
    public class EditTextDialogBuilder extends DefaultViewDialogBuilder {
        
        public EditTextDialogBuilder(Context context) {
            super(context, (ui = DialogEditTextBinding.inflate(LayoutInflater.from(context))).getRoot());
        }
        
        @Override
        public MaterialAlertDialogBuilder setPositiveButton(CharSequence sequence, OnClickListener listener) {
            return super.setPositiveButton(sequence, (dialog, which) -> {
                sendResult(ui.textInputEditText.getText().toString());
                    
                Optional.ofNullable(listener)
                    .ifPresent(listener1 -> listener1.onClick(dialog, which));
            });
        }
        
    }
}
