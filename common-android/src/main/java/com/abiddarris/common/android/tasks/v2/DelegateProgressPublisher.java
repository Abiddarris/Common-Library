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

public class DelegateProgressPublisher<P> implements ProgressPublisher<P> {

    private ProgressPublisher<P> progressPublisher = new NullProgressPublisher<>();
    private P progress;

    public DelegateProgressPublisher(ProgressPublisher<P> progressPublisher) {
        this.progressPublisher = progressPublisher;
    }

    public void setProgressPublisher(ProgressPublisher<P> progressPublisher) {
        this.progressPublisher = progressPublisher;

        if (this.progress != null) {
            this.progressPublisher.publish(this.progress);
        }
    }

    @Override
    public void publish(P progress) {
        this.progress = progress;

        progressPublisher.publish(progress);
    }

    @Override
    public void error(Throwable throwable) {
        progressPublisher.error(throwable);
    }

    @Override
    public void onFinish() {
        progressPublisher.onFinish();
    }
}
