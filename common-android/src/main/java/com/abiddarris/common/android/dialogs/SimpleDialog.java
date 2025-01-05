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

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Objects;

/**
 * Simple dialog that can show title, message and ok button
 *
 * <p> If {@code showForResult} or {@code showForResultAndBlock} is used on this dialog, the callback always be called if
 * the user click ok. The callback arguments always {@code null}.
 */
public class SimpleDialog extends BaseDialogFragment<Void> {
    
    private static final String DATA = "data";

    /**
     * Create new simple dialog
     *
     * @param title Dialog's title
     * @param message Dialog's message
     * @return Newly created simple dialog
     */
    public static SimpleDialog newSimpleDialog(@NonNull String title,
                                               @NonNull String message) {
        checkNonNull(title, "title cannot be null");
        checkNonNull(title, "message cannot be null");

        var dialog = new SimpleDialog();
        dialog.saveVariable(DATA, new String[] {title, message});

        return dialog;
    }
    
    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        setCancelable(false);

        String[] data = getVariable(DATA);
        
        builder.setTitle(data[0])
            .setMessage(data[1])
            .setPositiveButton(android.R.string.ok, (dialog, which) -> sendResult(null));
    }
 
    public static void show(FragmentManager manager, String title, String message) {
        newSimpleDialog(title, message).show(manager, null);
    }

    public static void showForResult(FragmentManager manager, String title, String message, Runnable runnable) {
        newSimpleDialog(title, message).showForResult(manager, (ignored) -> runnable.run());
    }
}
