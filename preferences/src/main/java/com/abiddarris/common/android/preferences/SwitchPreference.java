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

import android.view.View;

import com.abiddarris.common.android.preferences.databinding.LayoutSwitchPreferenceBinding;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SwitchPreference extends Preference {
    
    public SwitchPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
    }
    
    @Override
    protected View createView() {
        return LayoutSwitchPreferenceBinding.inflate(getFragment().getLayoutInflater())
            .getRoot();
    }
    
    @Override
    protected void fillView(View view) {
        super.fillView(view);
        
        MaterialSwitch materialSwitch = view.findViewById(R.id.materialSwitch);
        materialSwitch.setOnClickListener(v -> onSave(materialSwitch));
        materialSwitch.setChecked(getNonNullDataStore()
            .getBoolean(getKey()));
    }
    
    @Override
    protected void onClick(View view) {
        MaterialSwitch materialSwitch = view.findViewById(R.id.materialSwitch);
        materialSwitch.setChecked(!materialSwitch.isChecked());
        
        onSave(materialSwitch);
    }
    
    protected void onSave(MaterialSwitch materialSwitch) {
        storeBoolean(materialSwitch.isChecked());
    }
    
}
