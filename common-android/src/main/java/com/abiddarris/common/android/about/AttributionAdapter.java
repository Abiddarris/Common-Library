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

package com.abiddarris.common.android.about;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.abiddarris.common.android.about.AttributionAdapter.AttributionViewHolder;
import com.abiddarris.common.android.databinding.LayoutAttributionBinding;
import java.util.function.BiConsumer;

public class AttributionAdapter extends Adapter<AttributionViewHolder> {

    private Context context;
    private BiConsumer<Integer, Attribution> callback;
    private LayoutInflater inflater;
    private Attribution[] attributions;

    public AttributionAdapter(Context context, Attribution[] attributions) {
        this.context = context;
        this.attributions = attributions;

        inflater = LayoutInflater.from(context);
    }

    @Override
    public AttributionViewHolder onCreateViewHolder(ViewGroup group, int type) {
        return new AttributionViewHolder(LayoutAttributionBinding.inflate(inflater, group, false));
    }

    @Override
    public void onBindViewHolder(AttributionViewHolder holder, int index) {
        Attribution attribution = attributions[index];

        holder.binding.header.setText(attribution.getHeader());
        holder.binding.viewLicenseButton.setOnClickListener((view) -> {
            if(callback != null) {
                callback.accept(index, attribution);
            }
        });
    }

    @Override
    public int getItemCount() {
        return attributions.length;
    }
    
    public BiConsumer<Integer, Attribution> getCallback() {
        return this.callback;
    }

    public void setCallback(BiConsumer<Integer, Attribution> callback) {
        this.callback = callback;
    }

    public static class AttributionViewHolder extends ViewHolder {

        private LayoutAttributionBinding binding;

        private AttributionViewHolder(LayoutAttributionBinding binding) {
            super(binding.getRoot());

            this.binding = binding;
        }
    }

}
