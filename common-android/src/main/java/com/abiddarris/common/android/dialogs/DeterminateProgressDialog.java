/***********************************************************************************
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
 ***********************************************************************************/
package com.abiddarris.common.android.dialogs;

import static com.abiddarris.common.android.handlers.MainThreads.runOnMainThreadIfNot;
import static com.abiddarris.common.utils.Preconditions.checkNonNegative;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.abiddarris.common.android.databinding.DialogDetermineProgressBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class DeterminateProgressDialog extends BaseDialogFragment<Void> {

    private static final String TITLE = "title";
    private static final String MESSAGE = "message";
    public static final String MAX_PROGRESS = "maxProgress";
    public static final String PROGRESS = "progress";

    public static DeterminateProgressDialog newProgressDialog(String title) {
        DeterminateProgressDialog dialog = new DeterminateProgressDialog();
        dialog.setTitle(title);

        return dialog;
    }

    private DialogDetermineProgressBinding ui;

    @Override
    protected MaterialAlertDialogBuilder newDialogBuilder() {
        return new DetermineProgressDialogBuilder(getContext());
    }

    @Override
    protected void onCreateDialog(MaterialAlertDialogBuilder builder, Bundle savedInstanceState) {
        super.onCreateDialog(builder, savedInstanceState);

        if (getTitle() != null) {
            builder.setTitle(getTitle());
        }
    }

    @Override
    protected void onDialogCreated(AlertDialog dialog, Bundle savedInstanceState) {
        super.onDialogCreated(dialog, savedInstanceState);

        this.<CharSequence>observe(TITLE, dialog::setTitle, true);
        this.<CharSequence>observe(MESSAGE, ui.message::setText, true);
        this.observe(PROGRESS, ignored -> updateProgress(), true);
        this.observe(MAX_PROGRESS, ignored -> updateProgress(), true);
    }

    public void setTitle(@Nullable String title) {
        if (title == null) {
            title = "";
        }
        saveVariable(TITLE, title);
    }

    @Nullable
    private String getTitle() {
        return getVariable(TITLE, null);
    }

    public void addProgress(long progress) {
        setProgress(getProgress() + progress);
    }

    public void setProgress(long progress) {
        checkNonNegative(progress, "progress cannot be negative");
        saveVariable(PROGRESS, progress);
    }

    public long getProgress() {
        return getVariable(PROGRESS, 0L);
    }

    public void setMaxProgress(long maxProgress) {
        checkNonNegative(maxProgress, "maxProgress cannot be negative");
        saveVariable(MAX_PROGRESS, maxProgress);
    }

    private void updateProgress() {
        if (ui == null) {
            return;
        }
        runOnMainThreadIfNot(() -> {
            long maxProgress = getMaxProgress();
            long progress = getProgress();

            if (maxProgress > Integer.MAX_VALUE) {
                double divider = (double) maxProgress / Integer.MAX_VALUE;

                maxProgress = Integer.MAX_VALUE;
                progress = Math.round(progress / divider);
            }

            ui.progressBar.setMax((int)maxProgress);
            ui.progressBar.setProgress((int)progress);
            ui.percentage.setText(String.format("%.2f", (double)progress / maxProgress * 100) + "%");
        });
    }

    public long getMaxProgress() {
        return getVariable(MAX_PROGRESS, 0L);
    }

    public void setMessage(String message) {
        saveVariable(MESSAGE, message);
    }

    private class DetermineProgressDialogBuilder extends DialogBuilder {

        private DetermineProgressDialogBuilder(Context context) {
            super(context);

            ui = DialogDetermineProgressBinding.inflate(getLayoutInflater());

            super.setView(ui.getRoot());
        }

        @Override
        public DialogBuilder setView(View view) {
            throw new UnsupportedOperationException("Cannot set custom view on DetermineProgressDialog");
        }

    }

}
