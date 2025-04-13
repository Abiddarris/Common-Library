/* ────────────────────────────────────────────────────────────────────────
 * Copyright 2024-2025 Abiddarris
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
 * ────────────────────────────────────────────────────────────────────── */
package com.abiddarris.common.android.preferences;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.abiddarris.common.android.preferences.databinding.LayoutEditTextBinding;

public class EditTextPreference extends DialogPreference {

    private String defaultValue;
    private OnBindEditTextListener onBindEditTextListener;
    private LayoutEditTextBinding binding;

    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }

    @Override
    protected View onCreateView(LayoutInflater inflater) {
        String value = getValueOrDefault();
        binding = LayoutEditTextBinding.inflate(inflater);

        binding.textInput.getEditText().setText(value);
        binding.textInput.getEditText().requestFocus();
        
        if(getOnBindEditTextListener() != null)
            getOnBindEditTextListener().onBind(binding.textInput.getEditText());
        
        if(value != null) {
            binding.textInput.getEditText()
                .setSelection(value.length());
        }
        
        return binding.getRoot();
    }

    @Override
    protected void onSave() {
        super.onSave();

        storeString(binding.textInput.getEditText().getText().toString());
        refillView();
    }

    public String getValueOrDefault() {
        String value = getNonNullDataStore().getString(getKey());
        return value != null ? value : getDefaultValue();
    }

    public static class EditTextSummaryProvider implements SummaryProvider {

        private static final EditTextSummaryProvider summaryProvider =
                new EditTextSummaryProvider();

        @Override
        public String getSummary(Preference preference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            String value = editTextPreference.getValueOrDefault();
            return value == null ? "" : value;
        }

        public static EditTextSummaryProvider getInstance() {
            return summaryProvider;
        }
    }

    public static interface OnBindEditTextListener {
        void onBind(EditText editText);
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public OnBindEditTextListener getOnBindEditTextListener() {
        return this.onBindEditTextListener;
    }

    public void setOnBindEditTextListener(OnBindEditTextListener onBindEditTextListener) {
        this.onBindEditTextListener = onBindEditTextListener;
    }
}
