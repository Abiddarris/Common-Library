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
package com.abiddarris.common.android.dialogs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.abiddarris.common.android.R;
import com.abiddarris.common.android.databinding.DialogFragmentBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog that hosts a {@code Fragment}
 */
public class FragmentDialog<Result> extends BaseDialogFragment<Result> {
    
    private DialogFragmentBinding ui;
    
    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new DefaultViewDialogBuilder(getContext(), (ui = DialogFragmentBinding.inflate(getLayoutInflater())).getRoot());
    }
    
    @Override
    @MainThread
    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle savedInstanceState) {
        return ui.getRoot();
    }
    
    public void setFragment(Fragment fragment) {
        getChildFragmentManager()
            .beginTransaction()
            .setReorderingAllowed(true)
            .add(R.id.fragment, fragment, null)
            .commit();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends Fragment> T getFragment() {
        return isAdded() ? (T)getChildFragmentManager()
            .findFragmentById(R.id.fragment) : null;
    }
    
}
