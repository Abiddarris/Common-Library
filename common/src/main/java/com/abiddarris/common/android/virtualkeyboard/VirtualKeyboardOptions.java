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

import static android.widget.RelativeLayout.CENTER_HORIZONTAL;
import static android.widget.RelativeLayout.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;

import com.abiddarris.common.R;
import com.abiddarris.common.databinding.LayoutVirtualKeyboardOptionsBinding;
import com.google.android.material.button.MaterialButton;

public class VirtualKeyboardOptions extends LinearLayout {

    private LayoutVirtualKeyboardOptionsBinding binding;
    private String keyboardFolderPath;
    private String defaultSaveName;
    private VirtualKeyboard keyboard;

    public VirtualKeyboardOptions(Context context, VirtualKeyboard keyboard) {
        super(context);

        this.keyboard = keyboard;

        binding = LayoutVirtualKeyboardOptionsBinding.inflate(LayoutInflater.from(context), this);
        binding.edit.setOnClickListener(
                v -> {
                    keyboard.setEdit(!keyboard.isEdit());

                    int visibility = keyboard.isEdit() ? VISIBLE : GONE;

                    binding.add.setVisibility(visibility);
                    binding.setting.setVisibility(visibility);

                    ((MaterialButton)binding.edit).setIconResource(
                            keyboard.isEdit() ? R.drawable.ic_check : R.drawable.ic_edit);
                });

        binding.add.setOnClickListener(v -> keyboard.addButton());
        binding.setting.setOnClickListener(
                v -> {
                    var dialog = VirtualKeyboardSettingsDialog.newInstance(this);
                    dialog.show(
                            ((FragmentActivity) getContext()).getSupportFragmentManager(), null);
                });

        var params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        params.addRule(CENTER_HORIZONTAL);

        keyboard.addView(this, params);
    }

    public String getKeyboardFolderPath() {
        return this.keyboardFolderPath;
    }

    public void setKeyboardFolderPath(String keyboardFolderPath) {
        this.keyboardFolderPath = keyboardFolderPath;
    }

    public VirtualKeyboard getKeyboard() {
        return keyboard;
    }

    protected String getDefaultSaveName() {
        return this.defaultSaveName;
    }

    protected void setDefaultSaveName(String defaultSaveName) {
        this.defaultSaveName = defaultSaveName;
    }
}
