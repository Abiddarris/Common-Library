package com.abiddarris.common.android.preferences;

import android.content.SharedPreferences;

public interface PreferenceKeyChangedListener {
    void onPreferenceKeyChanged(SharedPreferences preference, String key);
}
