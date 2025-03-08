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

import static com.abiddarris.common.android.dialogs.ExceptionDialog.showExceptionDialog;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.abiddarris.common.android.tasks.v2.ProgressPublisher;

public abstract class DialogProgressPublisher<P, D extends DialogFragment> implements ProgressPublisher<P> {

    private final Object lock = new Object();
    private final String tag;

    private boolean dialogShown;
    private FragmentManager manager;
    private D dialog;
    private boolean valid;

    public DialogProgressPublisher(String tag) {
        this.tag = tag;
    }

    @Override
    public void publish(P progress) {
        ensureValid();
        if (dialogShown) {
            return;
        }
        dialogShown = true;

        (dialog = newDialog()).show(getFragmentManager(), tag);
    }

    @Override
    public void error(Throwable throwable) {
        ensureValid();

        showExceptionDialog(manager, throwable);
    }

    @Override
    public void onFinish() {
        ensureValid();

        getDialog().dismiss();
    }

    public abstract D newDialog();

    @SuppressWarnings("unchecked")
    public final D getDialog() {
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment == null && dialog != null) {
            return dialog;
        }

        if (fragment == null) {
            throw new IllegalStateException(String.format("DialogFragment with tag %s is not found in FragmentManager", tag));
        }

        return (D)fragment;
    }

    public final FragmentManager getFragmentManager() {
        ensureValid();
        synchronized (lock) {
            while (manager == null) {
                try {
                    lock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return manager;
        }
    }

    private void ensureValid() {
        if (!isValid()) {
            throw new IllegalStateException(getClass().getName() + " is not attached to any DialogProgressPublisherManager");
        }
    }

    boolean isValid() {
        return valid;
    }

    void setValid() {
        valid = true;
    }

    void attachFragmentManager(FragmentManager manager) {
        synchronized (lock) {
            this.manager = manager;
            lock.notifyAll();
        }
    }

    void markFragmentManagerInvalid() {
        synchronized (lock) {
            manager = null;
            dialog = null;
        }
    }
}
