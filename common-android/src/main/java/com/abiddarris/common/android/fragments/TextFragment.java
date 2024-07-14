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

import static android.text.util.Linkify.WEB_URLS;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;

import com.abiddarris.common.android.databinding.FragmentTextBinding;

/**
 * {@code Fragment} that contains {@code TextView} inside {@code ScrollView}
 */
public class TextFragment extends ScrollableFragment {
    
    private static final String TEXT = "text";
    private static final String HIGHLIGHT_LINK = "highlight_text";

    private FragmentTextBinding binding;
    private String text;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        binding = FragmentTextBinding.inflate(inflater, group, false);
        setViewToScroll(binding.getRoot());
        
        return super.onCreateView(inflater, group, bundle);
    }
    
    @Override
    @MainThread
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setScrollableVertically(true);
        setHighlightLinkInternal(getVariable(HIGHLIGHT_LINK, true));

        updateUI();
    }
    
    public void setText(String text) {
        saveVariable(TEXT, text);
        
        updateUI();
    }
    
    public String getText() {
        return getVariable(TEXT, "");
    }
    
    public void setHighlightLink(boolean highlight) {
        saveVariable(HIGHLIGHT_LINK, highlight);

        setHighlightLinkInternal(highlight);
    }

    public FragmentTextBinding getBinding() {
        return binding;
    }
    
    private void updateUI() {
        if(getBinding() != null) {
            getBinding().text.setText(getText());
        }
    }

    private void setHighlightLinkInternal(boolean highlight) {
        var binding = getBinding();
        if(binding == null) return;

        int value = highlight ? WEB_URLS : 0;
        if(binding.text.getAutoLinkMask() != value) {
            binding.text.setAutoLinkMask(value);
        }
    }

}
