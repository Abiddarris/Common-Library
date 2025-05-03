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

import androidx.annotation.Nullable;

import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ListPreference extends DialogPreference {

    private ListEntry[] entries = new ListEntry[0];
    private String defaultValue;

    public ListPreference(PreferenceFragment fragment, String key) {
        super(fragment, key, (preference) -> new ListPreferenceDialog());
    }

    public void setEntries(ListEntry... entries) {
        this.entries = entries;
    }
    
    public ListEntry[] getEntries() {
        return entries;
    }
    
    public String getValue() {
        return getNonNullDataStore().getString(getKey());
    }
    
    public String getValueOrDefault() {
        String value = getValue();
        
        return value != null ? value : getDefaultValue();
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public static class ListPreferenceSummaryProvider implements SummaryProvider {
       
        private static final ListPreferenceSummaryProvider provider = new ListPreferenceSummaryProvider();
        
        @Override
        public String getSummary(Preference preference) {
            ListPreference listPreferences = ((ListPreference)preference);
            String value = listPreferences.getValueOrDefault();
            
            for(ListEntry entry : listPreferences.getEntries()) {
            	if(entry.getValue().equals(value)) {
                    return entry.getTitle();
                }
            }
            return "";
        }
        
        public static ListPreferenceSummaryProvider getInstance() {
        	return provider;
        }
        
    }

    public static class ListPreferenceDialog extends BaseDialogFragment<Void> {

        private int selection;

        @Override
        protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
            super.onCreateDialog(builder, savedInstanceState);

            ListPreference preference = getListPreference();
            if (preference == null) {
                return;
            }
            String value = preference.getValueOrDefault();

            ListEntry[] entries = preference.getEntries();
            String[] choices = new String[entries.length];
            int selection = -1;
            for (int i = 0; i < choices.length; i++) {
                choices[i] = entries[i].getTitle();

                if(selection == -1 && entries[i].getValue().equals(value)) {
                    selection = i;
                }
            }

            builder.setTitle(preference.getTitle())
                    .setSingleChoiceItems(choices, selection,
                            (dialog, which) -> this.selection = which)
                    .setNegativeButton(android.R.string.cancel, (p1, p2) -> {})
                    .setPositiveButton(android.R.string.ok, (p1, p2) -> {
                        ListPreference listPreference = getListPreference();
                        listPreference.storeString(
                                listPreference.getEntries()[this.selection].getValue());
                        listPreference.refillView();
                    })
                    .create();
        }

        @Nullable
        private ListPreference getListPreference() {
            return getVariable(PREFERENCE);
        }
    }
}
