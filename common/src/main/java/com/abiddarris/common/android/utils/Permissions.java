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

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import com.abiddarris.common.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Permissions {
    
    public static void requestManageExternalStoragePermission(FragmentActivity activity, String message) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            RequestExternalStorageDialog.newInstance(message)
                .show(activity.getSupportFragmentManager(), null);
            return;
        }
        
        ActivityResultLauncher<String> requestPermissionLauncher =
            activity.registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            });
        
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    
    public static class RequestExternalStorageDialog extends DialogFragment {
        
        private static final String MESSAGE = "message";
        
        private ActivityResultLauncher<Intent> launcher;

        @Override
        public Dialog onCreateDialog(Bundle bundle) {
            var arguments = getArguments();
            
            launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), this::onResult);
            
            return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.permission_required)
                .setMessage(arguments.getString(MESSAGE))
                .setPositiveButton(R.string.grant, (dialog, which) -> openSettings())
                .create();
        }
        
        private void openSettings() {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getContext().getPackageName())));
                
                launcher.launch(intent);
            } catch (Exception e) {
                e.printStackTrace();
                
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
          
                launcher.launch(intent);
            }
        }
        
        private void onResult(ActivityResult result) {
        }
        
        private static RequestExternalStorageDialog newInstance(String message) {
            var bundle = new Bundle();
            bundle.putString(MESSAGE, message);
            
            var dialog = new RequestExternalStorageDialog();
            dialog.setArguments(bundle);
            
            return dialog;
        }
    }
}
