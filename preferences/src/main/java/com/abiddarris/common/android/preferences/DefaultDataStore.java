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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

@SuppressLint("ApplySharedPref")
public class DefaultDataStore implements DataStore {

    private final SharedPreferences preferences;

    public DefaultDataStore(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public String getString(String key) {
        return preferences.getString(key, null);
    }
    
    @Override
    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    @Override
    public void store(String key, String value) {
        preferences.edit()
            .putString(key, value)
            .commit();
    }
    
    @Override
    public void store(String key, boolean value) {
        preferences.edit()
            .putBoolean(key, value)
            .commit();
    }
    
}
