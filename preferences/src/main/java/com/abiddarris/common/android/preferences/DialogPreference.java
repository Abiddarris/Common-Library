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

import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.abiddarris.common.android.dialogs.BaseDialogFragment;

public class DialogPreference extends Preference {

    public static final String PREFERENCE = "preference";
    private final DialogFragmentFactory factory;

    public DialogPreference(PreferenceFragment fragment, String key, DialogFragmentFactory factory) {
        super(fragment, key);

        if (factory == null) {
            factory = new DefaultDialogFragmentFactory();
        }

        this.factory = factory;

        Fragment dialogFragment = getShownDialogFragment();
        if (dialogFragment == null) return;

        BaseDialogFragment<?> baseDialogFragment = (BaseDialogFragment<?>)dialogFragment;
        baseDialogFragment.saveVariable(PREFERENCE, this);
    }

    @Nullable
    private Fragment getShownDialogFragment() {
        Fragment dialogFragment = getFragment()
                .getChildFragmentManager()
                .findFragmentByTag(getKey());

        if (!(dialogFragment instanceof BaseDialogFragment)) {
            return null;
        }
        return dialogFragment;
    }

    @Override
    protected void onClick(View view) {
        super.onClick(view);

        if (getShownDialogFragment() != null) {
            return;
        }

        BaseDialogFragment<?> dialog = factory.createDialogFragment(this);
        dialog.saveVariable(PREFERENCE, this);
        dialog.show(getFragment().getChildFragmentManager(), getKey());
    }
    
}
