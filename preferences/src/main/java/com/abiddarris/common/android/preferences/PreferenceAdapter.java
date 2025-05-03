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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.abiddarris.common.android.preferences.databinding.LayoutPreferenceAdapterBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreferenceAdapter extends Adapter<PreferenceAdapter.PreferenceHolder> {

    private final LayoutInflater inflater;
    private final Preference[] preferences;

    PreferenceAdapter(Context context, Preference[] preferences) {
        List<Preference> preferencesList = new ArrayList<>();
        for(Preference preference : preferences) {
            preferencesList.add(preference);
            if (preference instanceof PreferenceCategory) {
                PreferenceCategory category = (PreferenceCategory)preference;
                Collections.addAll(preferencesList, category.getPreferences());
            }
        }
        
        this.preferences = preferencesList.toArray(new Preference[0]);
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public PreferenceHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new PreferenceHolder(LayoutPreferenceAdapterBinding.inflate(inflater).layout);
    }

    @Override
    public void onBindViewHolder(PreferenceHolder holder, int index) {
        holder.layout.removeAllViews();
        holder.layout.addView(preferences[index].getView(), new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT     
        ));
    }

    @Override
    public int getItemCount() {
        return preferences.length;
    }

    public static class PreferenceHolder extends ViewHolder {
        
        private final LinearLayout layout;
        
        public PreferenceHolder(LinearLayout layout) {
            super(layout);
            
            this.layout = layout;
        }
        
    }
}
