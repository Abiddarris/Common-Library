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
package com.abiddarris.common.android.tasks.v2.dialog;

import com.abiddarris.common.android.dialogs.ProgressDialog;
import com.abiddarris.common.android.tasks.v2.IndeterminateProgress;

public class IndeterminateDialogProgressPublisher extends DialogProgressPublisher<IndeterminateProgress, ProgressDialog> {

    public IndeterminateDialogProgressPublisher(String tag) {
        super(tag);
    }

    @Override
    public void publish(IndeterminateProgress progress) {
        super.publish(progress);

        getDialog().setTitle(progress.getTitle());
        getDialog().setMessage(progress.getMessage());
    }

    @Override
    public ProgressDialog newDialog() {
        ProgressDialog dialog = new ProgressDialog();
        dialog.setCancelable(false);

        return dialog;
    }
}
