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
package com.abiddarris.common.android.tasks.v2;

public abstract class BaseIndeterminateTask<T extends IndeterminateProgress, R> extends Task<T, R> {

    private T progressInstance;

    protected T getProgress() {
        if (progressInstance == null) {
            progressInstance = newProgressInstance();
        }

        return progressInstance;
    }

    protected abstract T newProgressInstance();

    protected void setTitle(int res) {
        setTitle(getString(res));
    }

    protected void setTitle(String title) {
        getProgress().setTitle(title);
        publish(progressInstance);
    }

    protected void setMessage(int message) {
        setMessage(getString(message));
    }

    protected void setMessage(String message) {
        getProgress().setMessage(message);
        publish(progressInstance);
    }
}
