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

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleEventObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceChangeDelegator
        implements LifecycleEventObserver, OnSharedPreferenceChangeListener {

    private Map<String, List<PreferenceKeyChangedListener>> listeners = new HashMap<>();
    private SharedPreferences preferences;

    public PreferenceChangeDelegator(SharedPreferences preferences) {
        this.preferences = preferences;
    }
    
    public void addListener(String key, PreferenceKeyChangedListener listener) {
    	getListeners(key)
            .add(listener);
    }
    
    public void removeListener(String key, PreferenceKeyChangedListener listener) {
    	getListeners(key)
            .remove(listener);
    }
    
    private synchronized List<PreferenceKeyChangedListener> getListeners(String key) {
        List<PreferenceKeyChangedListener> listeners = this.listeners.get(key);
        if(listeners == null) {
            listeners = new ArrayList<>();
            this.listeners.put(key, listeners);
        }
        return listeners;
    }

    @Override
    public void onStateChanged(LifecycleOwner owner, Event event) {
        switch (event) {
            case ON_RESUME:
                preferences.registerOnSharedPreferenceChangeListener(this);
                break;
            case ON_PAUSE :
                preferences.unregisterOnSharedPreferenceChangeListener(this);
        }    
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        List<PreferenceKeyChangedListener> listeners = getListeners(key);
        for(PreferenceKeyChangedListener listener : listeners) {
        	listener.onPreferenceKeyChanged(sharedPreferences, key);
        }
    }
}
