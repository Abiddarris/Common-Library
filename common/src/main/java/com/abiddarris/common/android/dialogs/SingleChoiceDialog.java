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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import com.abiddarris.common.R;
import com.abiddarris.common.databinding.DialogSingleChoiceBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.Optional;

public class SingleChoiceDialog extends BaseDialogFragment<Integer> {
    
    private boolean dialogNull;
    private boolean enablePositiveButton;
    
    private DialogSingleChoiceBinding ui;
    private int selection;
    private String[] items;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new SingleChoiceDialogBuilder(getContext());
    }
    
    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);
        
        if(dialogNull) {
            dialog.setOnShowListener(v -> enablePositiveButton(enablePositiveButton));
        }
    }
    
    protected void onSelected(int selection) {
        this.selection = selection;
        
        enablePositiveButton(selection >= 0);
    }
    
    public void setItems(String[] items, int selection) {
        this.items = items;
        
        var adapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_list_item_1, items);
        var spinner = (MaterialAutoCompleteTextView) ui.spinner.getEditText();
        spinner.setAdapter(adapter);
        
        String text = selection >= 0 ? items[selection] : getString(R.string.select_item);
        spinner.setText(text, false);
       
        onSelected(selection);
        
        spinner.setOnItemClickListener((adapterView, view, index, id) -> onSelected(index));
    }
    
    @Override
    protected Integer getDefaultResult() {
        return -1;
    }
    
    public String[] getItems() {
        return items;
    }
    
    public int getSelection() {
    	return selection;
    }
    
    private void enablePositiveButton(boolean enabled) {
        var dialog = (AlertDialog) getDialog();
        if(dialog == null) {
            dialogNull = true;
            enablePositiveButton = enabled;
            return;
        }
        
        Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(button == null) {
            dialog.setOnShowListener(v -> enablePositiveButton(enabled));
            return;
        }
        button.setEnabled(enabled);
    }
    
    
    private class SingleChoiceDialogBuilder extends DialogBuilder {
        
        private SingleChoiceDialogBuilder(Context context) {
            super(context);
            
            ui = DialogSingleChoiceBinding.inflate(getLayoutInflater());
        
            super.setView(ui.getRoot());
        }
        
        @Override
        @NonNull
        public MaterialAlertDialogBuilder setPositiveButton(int id, OnClickListener listener) {
            return setPositiveButton(getContext().getString(id), listener);
        }
        
        @Override
        @NonNull
        public MaterialAlertDialogBuilder setPositiveButton(CharSequence sequence, OnClickListener listener) {
            return super.setPositiveButton(sequence, (dialog, which) -> {
                sendResult(getSelection());
                Optional.ofNullable(listener)
                    .ifPresent(callback -> callback.onClick(dialog, getSelection()));
            });
        }
        
        @Override
        public DialogBuilder setView(View view) {
            throw new UnsupportedOperationException("Cannot set custom view on SingleChoiceDialog");
        }
        
    }

}
