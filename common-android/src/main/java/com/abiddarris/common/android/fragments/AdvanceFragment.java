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

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AdvanceFragment extends Fragment {
    
    private AdvanceFragmentDelegate delegate = new AdvanceFragmentDelegate();
    
    @Override
    @MainThread
    @CallSuper
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        delegate.onCreate(this);
    }
 
    @Nullable
    public <T> T getVariable(@Nullable String name) {
        return delegate.getVariable(name);
    }
    
    @Nullable
    public <T> T getVariable(@Nullable String name, T defaultVal) {
        return delegate.getVariable(name, defaultVal);
    }
    
    @Nullable
    public <T> T saveVariable(@Nullable String name, @Nullable T obj) {
        return delegate.saveVariable(name, obj);
    }
}
