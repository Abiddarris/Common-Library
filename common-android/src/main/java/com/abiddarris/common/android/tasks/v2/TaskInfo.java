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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TaskInfo<P, R> implements Future<R> {

    private final Set<OnTaskExecutedListener<R>> onTaskExecutedListeners = new LinkedHashSet<>();

    private DelegateProgressPublisher<P> delegateProgressPublisher;
    private Future<?> future;
    private R result;

    public R getResult() {
        return result;
    }

    public void setProgressPublisher(ProgressPublisher<P> progress) {
        delegateProgressPublisher.setProgressPublisher(progress);
    }

    public void addOnTaskExecuted(OnTaskExecutedListener<R> onTaskExecutedListener) {
        if (isDone()) {
            onTaskExecutedListener.onExecuted(result);
            return;
        }
        onTaskExecutedListeners.add(onTaskExecutedListener);
    }

    @Override
    public boolean cancel(boolean b) {
        return future.cancel(b);
    }

    @Override
    public boolean isCancelled() {
        return future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public R get() throws ExecutionException, InterruptedException {
        future.get();

        return result;
    }

    @Override
    public R get(long l, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        future.get(l, timeUnit);

        return result;
    }

    void setResult(R result) {
        this.result = result;
    }

    void setDelegateProgressPublisher(DelegateProgressPublisher<P> delegateProgressPublisher) {
        this.delegateProgressPublisher = delegateProgressPublisher;
    }

    void setFuture(Future<?> future) {
        this.future = future;
    }

    void queueTaskListener() {
        for (OnTaskExecutedListener<R> listener : onTaskExecutedListeners) {
            listener.onExecuted(result);
        }
    }
}
