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

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import android.content.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TaskManager {

    private final ExecutorService executors;

    private AtomicBoolean shutdown = new AtomicBoolean(false);
    private Context context;

    public TaskManager(Context context) {
        this(context, Executors.newSingleThreadExecutor());
    }

    public TaskManager(Context context, ExecutorService executors) {
        setContext(context);
        checkExecutorLive(executors);

        this.executors = executors;
    }

    private static void checkExecutorLive(ExecutorService executors) {
        if (executors.isShutdown()) {
            throw new IllegalArgumentException("executors already shutdown");
        }
    }

    public <P, R> TaskInfo<P, R> execute(Task<P, R> task, ProgressPublisher<P> publisher) {
        ensureLive();
        if (publisher == null) {
            publisher = new NullProgressPublisher<>();
        }

        TaskInfo<P, R> taskInfo = new TaskInfo<>();
        task.attach(this, taskInfo);

        taskInfo.setProgressPublisher(publisher);

        checkExecutorLive(executors);
        taskInfo.setFuture(executors.submit(task));

        return taskInfo;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        checkNonNull(context, "context cannot be null");

        this.context = context;
    }

    public void shutdown() {
        shutdown(false);
    }

    public void shutdown(boolean now) {
        if (isShutdown()) {
            return;
        }

        if (now) {
            executors.shutdownNow();
        } else {
            executors.shutdown();
        }

        shutdown.set(true);
    }

    public boolean isShutdown() {
        return shutdown.get();
    }

    private void ensureLive() {
        if (isShutdown()) {
            throw new IllegalStateException("TaskManager already shutdown");
        }
    }
}
