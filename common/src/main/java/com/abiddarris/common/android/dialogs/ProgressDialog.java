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
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.abiddarris.common.databinding.DialogProgressBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * {@code Dialog} that show indetermine progress
 */
public class ProgressDialog extends BaseDialogFragment<Void> {
    
    public static final String MESSAGE = "message";
    
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    
    private DialogProgressBinding ui;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new ProgressDialogBuilder(getContext());
    }
    
    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);
        
        updateUI();
    }
    
    public void setMessage(String message) {
        saveVariable(MESSAGE, message);
        
        if(ui != null) {
            HANDLER.post(() -> updateUI());
        }
    }
    public String getMessage() {
        return getVariable(MESSAGE);
    }
    
    private void updateUI() {
        ui.message.setText(getMessage());
    }
    
    private class ProgressDialogBuilder extends DialogBuilder {
        
        private ProgressDialogBuilder(Context context) {
            super(context);
            
            ui = DialogProgressBinding.inflate(getLayoutInflater());
        
            super.setView(ui.getRoot());
        }
        
        @Override
        public DialogBuilder setView(View view) {
            throw new UnsupportedOperationException("Cannot set custom view on SingleChoiceDialog");
        }
        
    }

}
