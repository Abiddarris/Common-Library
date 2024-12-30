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
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog that display title, message, positive and negative button.
 *
 * <p> Return true if user click the postive button, otherwise false
 */
public final class SimpleConfirmationDialog extends BaseDialogFragment<Boolean> {

    private static final String DATA = "data";

    private SimpleConfirmationDialog() {
    }

    /**
     * Create new confirmation dialog
     *
     * @param title the dialog's title
     * @param message the dialog's message
     * @param no the dialog's negative button label
     * @param yes the dialog's postiive button label
     * @return Newly created confirmation dialog
     */
    @NonNull
    public static SimpleConfirmationDialog newConfirmationDialog(
            @NonNull String title, @NonNull String message,
            @Nullable String no, @Nullable String yes) {
        checkNonNull(title, "title cannot be null");
        checkNonNull(message, "message cannot be null");

        var dialog = new SimpleConfirmationDialog();
        dialog.saveVariable(DATA, new String[] {
                title, message, no, yes
        });
        return dialog;
    }

    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);

        String[] data = getVariable(DATA);

        String no = data[2] == null ? getString(android.R.string.no) : data[2];
        String yes = data[3] == null ? getString(android.R.string.yes) : data[3];

        setCancelable(false);

        builder.setTitle(data[0])
                .setMessage(data[1])
                .setNegativeButton(no, (dialog, which) -> sendResult(false))
                .setPositiveButton(yes, (dialog, which) -> sendResult(true));
    }
}
