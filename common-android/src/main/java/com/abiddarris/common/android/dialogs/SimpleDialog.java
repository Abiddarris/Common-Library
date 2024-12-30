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
import androidx.fragment.app.FragmentManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SimpleDialog extends BaseDialogFragment {
    
    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        var args = getArguments();
        
        builder.setTitle(args.getString(TITLE))
            .setMessage(args.getString(MESSAGE))
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {});
    }
 
    public static void show(FragmentManager manager, String title, String message) {
    	var args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, message);
        
        var dialog = new SimpleDialog();
        dialog.setArguments(args);
        dialog.show(manager, null);
    }
}
