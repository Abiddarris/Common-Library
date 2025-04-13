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

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogFragmentPreference extends DialogFragment {
    
    private DialogPreference preference;
    
    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        String key = getArguments()
            .getString(DialogPreference.KEY);
        
        preference = new ViewModelProvider(requireActivity())
            .get(DialogCommunicator.class)
            .find(key);
        
        return preference.onCreateDialog(this);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        preference.onDialogDestroy();
        
        super.onDestroy();
    }
    
    
}
