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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.HashMap;
import java.util.Map;

public class AdvanceFragmentDelegate {
    
    private PersistentViewModel model;
    private Map<String, Object> variables = new HashMap<>();
    
    public void onCreate(Fragment fragment) {
        model = new ViewModelProvider(fragment)
            .get(PersistentViewModel.class);
        variables = model.attach(variables);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getVariable(@Nullable String name) {
        return getVariable(name, null);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getVariable(@Nullable String name, T defaultVal) {
        return (T)variables.getOrDefault(name, defaultVal);
    }
    
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T saveVariable(@Nullable String name, @Nullable T obj) {
        return (T)variables.put(name, obj);
    }
    
    public static class PersistentViewModel extends ViewModel {
        
        private Map<String, Object> variables = null;
        
        private Map<String, Object> attach(Map<String, Object> variables) {
            if(this.variables == null) {
                this.variables = variables;
            }
            return this.variables;
        }
        
    }
    
}
