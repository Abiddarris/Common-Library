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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import static com.abiddarris.common.android.virtualkeyboard.Alignment.BOTTOM;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.LEFT;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.RIGHT;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.TOP;
import static com.abiddarris.common.android.virtualkeyboard.Size.AUTO;
import static com.abiddarris.common.android.virtualkeyboard.Size.CUSTOM;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.abiddarris.common.R;
import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.abiddarris.common.android.dialogs.ExceptionDialog;
import com.abiddarris.common.databinding.DialogEditButtonBinding;
import com.abiddarris.common.utils.Locales;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class EditButtonDialog extends BaseDialogFragment<Void> {
    
    private static final String FOCUS = "focus";
    
    private DialogEditButtonBinding binding;
    private int alignmentIndex;
    private int sizeType;
    private NumberFormat numberFormattor;
    private Keycode code;
    
    public static EditButtonDialog newInstance(Key key) {
        var dialog = new EditButtonDialog();
        dialog.saveVariable(FOCUS, key);
        
        return dialog;
    }
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
       
        Key key = getVariable(FOCUS);
        Button button = key.getButton();
        
        binding = DialogEditButtonBinding.inflate(getLayoutInflater());
        binding.name.getEditText()
            .setText(button.getText());
       
        code = key.getKeycode();
        
        var adapter = new KeySpinner(getContext());
        
        MaterialAutoCompleteTextView keySpinner = (MaterialAutoCompleteTextView) binding.key.getEditText();
        keySpinner.setText(code == null ? getString(R.string.select_item) : code.name());
        keySpinner.setAdapter(adapter);
        keySpinner.setOnItemClickListener((adapterView, view, index, id) -> code = adapter.getItem(index));
        
        Alignment alignment = key.getAlignment();
        alignment.calculate();
        
        int alignmentId;
        switch(alignment.getFlags()) {
            case Alignment.LEFT | Alignment.TOP :
                alignmentId = R.string.left;
                break;
            case Alignment.LEFT | Alignment.BOTTOM :
                alignmentId = R.string.left_and_bottom;
                break;
            case Alignment.RIGHT | Alignment.TOP :
                alignmentId = R.string.right;
                break;
            default :
                alignmentId = R.string.right_and_bottom;
        }
        
        MaterialAutoCompleteTextView alignmentSpinner = (MaterialAutoCompleteTextView)binding.alignment.getEditText();
        alignmentSpinner.setText(alignmentId);
        alignmentSpinner.setSimpleItems(R.array.alignment);
        alignmentSpinner.setOnItemClickListener((adapterView, view, index, id) -> alignmentIndex = index);
        
        numberFormattor = NumberFormat.getInstance(
            Locales.getPrimaryLocale(getContext())
        );
        
        binding.marginX.getEditText()
            .setText(numberFormattor.format(alignment.getMarginX()));
        binding.marginY.getEditText()
            .setText(numberFormattor.format(alignment.getMarginY()));
        
        Size size = key.getSize();
        size.calculate();
        
        int sizeId = getSizeId(size.getType());
        
        binding.width.getEditText()
            .setText(numberFormattor.format(size.getWidth()));
        binding.height.getEditText()
            .setText(numberFormattor.format(size.getHeight()));
        
        MaterialAutoCompleteTextView sizeSpinner = (MaterialAutoCompleteTextView)binding.size.getEditText();
        sizeSpinner.setText(sizeId);
        sizeSpinner.setSimpleItems(R.array.size_choices);
        sizeSpinner.setOnItemClickListener((adapterView, view, index, id) -> handleSizeSpinnerChanged(index));
        
        builder.setTitle(R.string.edit)
            .setView(binding.getRoot())
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                button.setText(binding.name.getEditText().getText().toString());
                
                key.setKeycode(code);
                
                alignment.setMargins(
                    getAlignmentFlag(),
                    editTextToFloat(binding.marginX),
                    editTextToFloat(binding.marginY)
                );
                
                size.setType(sizeType);
                if(sizeType == CUSTOM) {
                    size.setSize(
                        editTextToFloat(binding.width),
                        editTextToFloat(binding.height)
                    );
                }
            });
    }
    
    private int getSizeId(int type) {
        int sizeId;
        switch(type) {
            case Size.AUTO :
                sizeId = R.string.auto;
                sizeType = AUTO;
                break;
            default :
                sizeId = R.string.custom;
                sizeType = CUSTOM;
            
                binding.width.setVisibility(View.VISIBLE);
                binding.height.setVisibility(View.VISIBLE);
        }
        return sizeId;
    }
    
    private void handleSizeSpinnerChanged(int index) {
        int sizeVisibility = -1;
        switch(index) {
            case 0 :
                sizeVisibility = GONE;
                sizeType = AUTO;
                break;
            case 1 :
                sizeVisibility = VISIBLE;
                sizeType = CUSTOM;
        }
        
        binding.width.setVisibility(sizeVisibility);
        binding.height.setVisibility(sizeVisibility);
    }
    
    private float editTextToFloat(TextInputLayout layout) {
        try {
            return numberFormattor.parse(
                layout.getEditText()
                    .getText()
                    .toString())
                .floatValue();
        } catch (ParseException e) {
            var dialog = new ExceptionDialog();
            dialog.setThrowable(e);
            dialog.show(getParentFragmentManager(), null);
            
            return 0;
        }
    }
    
    private int getAlignmentFlag() {
        switch(alignmentIndex) {
            case 1 :
                return LEFT | BOTTOM;
            case 2 :
                return RIGHT | TOP;
            case 3 :
                return RIGHT | BOTTOM;
            default :
                return LEFT | TOP;
        }
    }
    
}
