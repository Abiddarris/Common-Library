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

import android.os.Handler;

public class DelayedProgressPublisher<P> implements ProgressPublisher<P> {

    private final Handler handler = new Handler();
    private final long delayed;
    private final ProgressPublisher<P> publisher;
    private final Runnable progressDispatchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!dispatcherRunning) {
                return;
            }
            synchronized (DelayedProgressPublisher.this) {
                if (progress != null) {
                    publisher.publish(progress);
                }
            }
            handler.postDelayed(progressDispatchRunnable, delayed);
        }
    };

    private volatile P progress;
    private volatile boolean dispatcherRunning;

    public DelayedProgressPublisher(ProgressPublisher<P> publisher) {
        this(publisher, 1000);
    }

    public DelayedProgressPublisher(ProgressPublisher<P> publisher, long delayed) {
        this.publisher = publisher;
        this.delayed = delayed;
    }

    @Override
    public void publish(P progress) {
        if (!dispatcherRunning) {
            handler.post(progressDispatchRunnable);
            dispatcherRunning = true;
        }
        synchronized (this) {
            this.progress = progress;
        }
    }

    @Override
    public void error(Throwable throwable) {
        stopDispatcher();
        publisher.error(throwable);
    }

    @Override
    public void onFinish() {
        stopDispatcher();
        publisher.onFinish();
    }

    private void stopDispatcher() {
        dispatcherRunning = false;
        handler.removeCallbacks(progressDispatchRunnable);
    }
}
