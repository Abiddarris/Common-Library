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
package com.abiddarris.common.android.utils;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.abiddarris.common.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Permissions {
    
    public static void requestManageExternalStoragePermission(FragmentManager manager, String message) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            RequestExternalStorageDialog.newInstance(message)
                .show(manager, null);
            return;
        }
    }
    
    public static class RequestExternalStorageDialog extends DialogFragment {
        
        private static final String MESSAGE = "message";
        
        private static RequestExternalStorageDialog newInstance(String message) {
            var bundle = new Bundle();
            bundle.putString(MESSAGE, message);
            
            var dialog = new RequestExternalStorageDialog();
            dialog.setArguments(bundle);
            
            return dialog;
        }

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            var arguments = getArguments();
            
            return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.permission_required)
                .setMessage(arguments.getString(MESSAGE))
                .setPositiveButton(R.string.grant, (dialog, which) -> {})
                .create();
        }
        
    }
}
