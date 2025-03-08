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

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import java.util.HashSet;
import java.util.Set;

public class DialogProgressPublisherManager {

    private final Set<DialogProgressPublisher<?, ?>> publishers = new HashSet<>();
    private final Object lock = new Object();

    private FragmentManager manager;

    public DialogProgressPublisherManager(FragmentActivity activity) {
        attach(activity);
    }

    public DialogProgressPublisherManager(Fragment fragment) {
        attach(fragment);
    }

    public void registerPublisher(DialogProgressPublisher<?, ?> publisher) {
        if (publishers.contains(publisher)) {
            return;
        }

        if (publisher.isValid()) {
            throw new IllegalArgumentException("publisher already attached to other manager");
        }

        publishers.add(publisher);

        publisher.attachFragmentManager(getFragmentManager());
        publisher.setValid();
    }

    public void attach(FragmentActivity activity) {
        attachInternal(activity.getSupportFragmentManager());
    }

    private void attach(Fragment fragment) {
        attachInternal(fragment.getChildFragmentManager());
    }

    private void attachInternal(FragmentManager manager) {
        synchronized (lock) {
            this.manager = manager;
            for (var publisher : publishers) {
                publisher.attachFragmentManager(manager);
            }
            lock.notifyAll();
        }
    }

    private FragmentManager getFragmentManager() {
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

    public void invalidate() {
        synchronized (lock) {
            manager = null;
            for (var publisher : publishers) {
                publisher.markFragmentManagerInvalid();
            }
        }
    }

}
