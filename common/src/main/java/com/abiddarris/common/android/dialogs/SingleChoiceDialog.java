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
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.abiddarris.common.R;
import com.abiddarris.common.android.utils.ItemSelectedListener;
import com.abiddarris.common.databinding.DialogSingleChoiceBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import java.util.Optional;

public class SingleChoiceDialog extends BaseDialogFragment<Integer> {
    
    private DialogSingleChoiceBinding ui;
    private int selection;
    private String[] items;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new SingleChoiceDialogBuilder(getContext());
    }
    
    protected void onSelected(int selection) {
        this.selection = selection;
    }
    
    public void setItems(String[] items, int selection) {
        this.items = items;
        
        var adapter = new ArrayAdapter<>(
            getContext(), android.R.layout.simple_list_item_1, items);
        var spinner = (MaterialAutoCompleteTextView) ui.spinner.getEditText();
        spinner.setAdapter(adapter);
        
        onSelected(selection);
        
        spinner.setOnItemClickListener((adapterView, view, index, id) -> onSelected(index));
    }
    
    @Override
    protected Integer getDefaultResult() {
        return getSelection();
    }
    
    public String[] getItems() {
        return items;
    }
    
    public int getSelection() {
    	return selection;
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
            return super.setPositiveButton(sequence, (dialog, which) -> Optional.ofNullable(listener)
                .ifPresent(callback -> callback.onClick(dialog, getSelection())));
        }
        
        @Override
        public DialogBuilder setView(View view) {
            throw new UnsupportedOperationException("Cannot set custom view on SingleChoiceDialog");
        }
        
    }
}
