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

import static com.abiddarris.common.android.utils.ScreenUtils.pixelToDp;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.BOTTOM;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.LEFT;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.RIGHT;
import static com.abiddarris.common.android.virtualkeyboard.Alignment.TOP;
import static com.abiddarris.common.android.virtualkeyboard.Size.AUTO;
import static com.abiddarris.common.android.virtualkeyboard.Size.CUSTOM;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.abiddarris.common.android.R;
import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.abiddarris.common.android.dialogs.ExceptionDialog;
import com.abiddarris.common.android.utils.TextListener;
import com.abiddarris.common.android.validations.ValidationEngine;
import com.abiddarris.common.android.validations.Validator;
import com.abiddarris.common.android.databinding.DialogEditButtonBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditButtonDialog extends BaseDialogFragment<Void> {
    
    private static final String VIRTUAL_KEYBOARD = "keyboard";
    
    private DialogEditButtonBinding binding;
    private int alignmentIndex;
    private int sizeType;
    private Keycode code;
    private ValidationEngine validationEngine = new ValidationEngine();
    
    public static EditButtonDialog newInstance(VirtualKeyboard keyboard) {
        var dialog = new EditButtonDialog();
        dialog.saveVariable(VIRTUAL_KEYBOARD, keyboard);
        
        return dialog;
    }
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);
        setCancelable(false);
        
        validationEngine.setValidationChangedListener(this::enablePositiveButton);
       
        VirtualKeyboard keyboard = getVariable(VIRTUAL_KEYBOARD);
        Key key = keyboard.getFocus();
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
        
        int alignmentId = getAlignmentId(alignment.getFlags());
        
        MaterialAutoCompleteTextView alignmentSpinner = (MaterialAutoCompleteTextView)binding.alignment.getEditText();
        alignmentSpinner.setText(alignmentId);
        alignmentSpinner.setSimpleItems(R.array.alignment);
        alignmentSpinner.setOnItemClickListener((adapterView, view, index, id) -> alignmentIndex = index);

        Size size = key.getSize();
        size.calculate();

        float originalWidth = size.getWidth(),
              originalHeight = size.getHeight();

        setValue(binding.marginX, alignment.getMarginX());
        setValue(binding.marginY, alignment.getMarginY());
        setValidation(binding.marginX, (text) -> {
            float x = toFloat(text);
            if(x > pixelToDp(getContext(), keyboard.getWidth()) - editTextToFloat(binding.width) || x < 0) {
                return getString(R.string.out_of_bounds_error);
            }
            return null;
        });
        setValidation(binding.marginY, (text) -> {
            float y = toFloat(text);
            if(y > pixelToDp(getContext(), keyboard.getHeight()) - editTextToFloat(binding.width) || y < 0) {
                return getString(R.string.out_of_bounds_error);
            }
            return null;
        });
        setValidation(binding.width, (text) -> {
            float width = toFloat(text);
            float marginX = editTextToFloat(binding.marginX);
            float maxSize = pixelToDp(getContext(), keyboard.getWidth());
            float max = (getAlignmentFlag() & RIGHT) == 0 ? maxSize - marginX : marginX + originalWidth;

            if(width > max || width <= 0) {
                return getString(R.string.out_of_bounds_error);
            }
            return null;
        });
        setValidation(binding.height, (text) -> {
            float height = toFloat(text);
            float marginY = editTextToFloat(binding.marginY);
            float maxSize = pixelToDp(getContext(), keyboard.getHeight());
            float max = (getAlignmentFlag() & BOTTOM) == 0 ? maxSize - marginY : marginY + originalHeight;

            if(height > max || height <= 0) {
                return getString(R.string.out_of_bounds_error);
            }
            return null;
        });


        int sizeId = getSizeId(size.getType());
        
        setValue(binding.width, originalWidth);
        setValue(binding.height, originalHeight);
        
        MaterialAutoCompleteTextView sizeSpinner = (MaterialAutoCompleteTextView)binding.size.getEditText();
        sizeSpinner.setText(sizeId);
        sizeSpinner.setSimpleItems(R.array.size_choices);
        sizeSpinner.setOnItemClickListener((adapterView, view, index, id) -> handleSizeSpinnerChanged(index));
        
        builder.setTitle(R.string.edit)
            .setView(binding.getRoot())
            .setNeutralButton(R.string.delete, (dialog, which) -> keyboard.removeButton(key))
            .setNegativeButton(android.R.string.cancel, null)
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
    
    private void setValidation(TextInputLayout layout, Validator validator) {
        validationEngine.addEditText(layout, validator);
    }
    
    private void setValue(TextInputLayout textInput, float value) {
        textInput.getEditText()
            .setText(String.valueOf(value));
    }
    
    private int getAlignmentId(int flags) {
        int alignmentId;
        switch(flags) {
            case LEFT | BOTTOM :
                alignmentId = R.string.left_and_bottom;
                alignmentIndex = 1;
                break;
            case RIGHT | TOP :
                alignmentId = R.string.right;
                alignmentIndex = 2;
                break;
            case RIGHT | BOTTOM :
                alignmentId = R.string.right_and_bottom;
                alignmentIndex = 3;
                break;
            default :
                alignmentId = R.string.left;
                alignmentIndex = 0;
        }
        return alignmentId;
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
    
    private float toFloat(String string) {
        if(string.isBlank()) {
            return 0;
        }
        return Float.valueOf(string);
    }
    
    private float editTextToFloat(TextInputLayout layout) {
        return toFloat(layout.getEditText()
                    .getText()
                    .toString());
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
