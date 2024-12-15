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

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_DENIED;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.abiddarris.common.R;
import com.abiddarris.common.android.dialogs.BaseDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.function.Consumer;

public class Permissions {

    @Deprecated
    public static boolean checkPermission(Context context, String... permissions) {
        return checkPermissions(context, permissions);
    }

    public static boolean checkPermissions(Context context, String... permissions) {
        for(var permission : permissions) {
        	if(ContextCompat.checkSelfPermission(context, permission) == PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isManageExternalStorageGranted(Context context) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        }
        
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R ?
            Environment.isExternalStorageManager() :
            checkPermission(context, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
    }
    
    public static void requestPermissions(FragmentActivity activity, Consumer<Boolean> callback, String... permissions) {
        ActivityResultLauncher<String[]> requestPermissionLauncher =
                 activity.registerForActivityResult(new RequestMultiplePermissions(), result ->
                        callback.accept(result.values()
                                .stream()
                                .reduce((p1, p2) -> p1 && p2)
                                .get()));
        requestPermissionLauncher.launch(permissions);
    }
    
    @TargetApi(16)
    public static void requestManageExternalStoragePermission(FragmentActivity activity, String message, Consumer<Boolean> callback) {
        if(isManageExternalStorageGranted(activity)) {
            return;
        }
        
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requestPermissions(activity, callback, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE);
            return;
        }
        
        ActivityResultLauncher<Intent> launcher = activity.registerForActivityResult(
            new StartActivityForResult(), result -> callback.accept(isManageExternalStorageGranted(activity)));
            
        var dialog = new RequestExternalStorageDialog();
        dialog.saveVariable(RequestExternalStorageDialog.MESSAGE, message);
        dialog.showForResult(activity.getSupportFragmentManager(), result -> {
            if(!result) {
                callback.accept(false);   
                return;
            }
                
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", activity.getPackageName())));
                
                launcher.launch(intent);
            } catch (Exception e) {
                e.printStackTrace();
                
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
          
                launcher.launch(intent);
            }    
        });
    }
    
    public static class RequestExternalStorageDialog extends BaseDialogFragment<Boolean> {
        
        private static final String MESSAGE = "message";
        
        @Override
        protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
            super.onCreateDialog(builder, savedInstanceState);
            
            builder.setTitle(R.string.permission_required)
                .setMessage(getVariable(MESSAGE))
                .setPositiveButton(R.string.grant, (dialog, which) -> sendResult(true));
        }
        
        @Override
        @MainThread
        @CallSuper
        public void onDestroy() {
            super.onDestroy();
            
            dismiss();
        }
        
        @Nullable
        @Override
        protected Boolean getDefaultResult() {
            return false;
        }
        
    }
}
