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

import com.abiddarris.common.android.preferences.databinding.LayoutCategoryBinding;

import java.util.ArrayList;
import java.util.List;

public class PreferenceCategory extends Preference {

    private final List<Preference> preferences = new ArrayList<>();

    public PreferenceCategory(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    public void addPreference(Preference... preferences) {
        for (Preference preference : preferences) {
            if (preference instanceof PreferenceCategory)
                throw new IllegalArgumentException("preference cannot be a category!");
            
            this.preferences.add(preference);
        }
    }
    
    public Preference[] getPreferences() {
        return preferences.toArray(new Preference[0]);
    }
    
    @Override
    protected View createView() {
        return LayoutCategoryBinding.inflate(LayoutInflater
            .from(getFragment().getContext()))
            .getRoot();
    }
    
    @Override
    protected void fillView(View view) {
        LayoutCategoryBinding binding = LayoutCategoryBinding.bind(view);
        binding.title.setText(getTitle());
    }
    
}
