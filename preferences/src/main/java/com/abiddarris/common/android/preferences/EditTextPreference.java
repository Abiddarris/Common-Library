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

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.abiddarris.common.android.databinding.DialogEditTextBinding;
import com.abiddarris.common.android.dialogs.EditTextDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class EditTextPreference extends DialogPreference {

    private String defaultValue;
    private OnBindEditTextListener onBindEditTextListener;

    public EditTextPreference(PreferenceFragment fragment, String key) {
        super(fragment, key, (preference) -> new EditTextDialogImpl());
    }

    public String getValueOrDefault() {
        String value = getNonNullDataStore().getString(getKey());
        return value != null ? value : getDefaultValue();
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

    public static class EditTextDialogImpl extends EditTextDialog {

        @Override
        protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
            super.onCreateDialog(builder, savedInstanceState);

            EditTextPreference preference = getEditTextPreference();
            if (preference == null) {
                dismiss();
                return;
            }

            String value = preference.getValueOrDefault();
            setText(value);

            DialogEditTextBinding ui = getUI();
            if (value != null) {
                ui.textInputEditText.setSelection(value.length());
            }
            ui.textInputEditText.requestFocus();

            if(preference.getOnBindEditTextListener() != null) {
                preference.getOnBindEditTextListener().onBind(ui.textInputEditText);
            }

            builder.setTitle(preference.getTitle())
                    .setNegativeButton(android.R.string.cancel, (d, w) -> {})
                    .setPositiveButton(android.R.string.ok, (d, w) -> {
                        EditTextPreference editTextPreference = getEditTextPreference();
                        editTextPreference.storeString(getText());
                        editTextPreference.refillView();
                    });
        }

        @Nullable
        private EditTextPreference getEditTextPreference() {
            return getVariable(PREFERENCE);
        }

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

    public interface OnBindEditTextListener {
        void onBind(EditText editText);
    }
}
