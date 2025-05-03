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
import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abiddarris.common.android.preferences.databinding.FragmentPreferenceBinding;

public abstract class PreferenceFragment extends Fragment {

    private DataStore defaultDataStore;

    public PreferenceFragment() {
        super(R.layout.fragment_preference);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        
        setDefaultDataStore(new DefaultDataStore(getContext()));
    }
    
    @Override
    @MainThread
    public final void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        PreferenceAdapter adapter = new PreferenceAdapter(getContext(), onCreatePreference());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        FragmentPreferenceBinding binding = FragmentPreferenceBinding.bind(view);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    public DataStore getDefaultDataStore() {
        return this.defaultDataStore;
    }

    public void setDefaultDataStore(DataStore defaultDataStore) {
        if(defaultDataStore == null) {
            throw new NullPointerException("DataStore cannot be null!");
        }
        
        this.defaultDataStore = defaultDataStore;
    }

    public abstract Preference[] onCreatePreference();

}
