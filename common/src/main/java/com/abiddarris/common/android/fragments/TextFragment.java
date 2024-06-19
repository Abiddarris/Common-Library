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
package com.abiddarris.common.android.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import com.abiddarris.common.databinding.FragmentTextBinding;

/**
 * {@code Fragment} that contains {@code TextView} inside {@code NestedScrollView}
 */
public class TextFragment extends Fragment {
    
    private static final String TEXT = "text";
    
    private FragmentTextBinding binding;
    private String text;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        binding = FragmentTextBinding.inflate(inflater, group, false);
        
        return binding.getRoot();
    }
    
    @Override
    @MainThread
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if(savedInstanceState != null) {
            setText(savedInstanceState.getString(TEXT));
            return;
        }
       
        updateUI();
    }
    
    @Override
    @MainThread
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        
        bundle.putString(TEXT, text);
    }
    
    public void setText(String text) {
        this.text = text;
        
        updateUI();
    }
    
    private void updateUI() {
        if(getBinding() != null)   
            getBinding().text.setText(text);
    }
    
    public FragmentTextBinding getBinding() {
        return binding;
    }
    
}
