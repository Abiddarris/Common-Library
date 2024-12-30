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
import com.abiddarris.common.android.R;
import com.abiddarris.common.android.databinding.DialogSingleChoiceBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.Optional;

public class SingleChoiceDialog extends BaseDialogFragment<Integer> {
    
    private static final String ITEMS = "items";
    private static final String SELECTION = "selection";
    
    private DialogSingleChoiceBinding ui;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new SingleChoiceDialogBuilder(getContext());
    }
    
    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);
        
        var items = getItems();
        if(items != null)
            fillUIWithItems(items, getSelection());
    }
    
    protected void onSelected(int selection) {
        saveVariable(SELECTION, selection);
        
        enablePositiveButton(selection >= 0);
    }
    
    public void setItems(String[] items, int selection) {
        saveVariable(ITEMS, items);
        onSelected(selection);
        
        fillUIWithItems(items, selection);
    }
    
    @Override
    protected Integer getDefaultResult() {
        return -1;
    }
    
    public String[] getItems() {
        return getVariable(ITEMS);
    }
    
    public int getSelection() {
    	return getVariable(SELECTION, -1);
    }
    
    private void fillUIWithItems(String[] items, int selection) {
        var context = getContext();
        if(context == null) {
            return;
        }
        
        var adapter = new ArrayAdapter<>(context, R.layout.layout_item, items);
        var spinner = (MaterialAutoCompleteTextView) ui.spinner.getEditText();
        spinner.setAdapter(null);
        
        String text = selection >= 0 ? items[selection] : getString(R.string.select_item);
        
        spinner.setText(text);
        spinner.setAdapter(adapter);
        spinner.setOnItemClickListener((adapterView, view, index, id) -> onSelected(index));
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
