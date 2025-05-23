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
package com.abiddarris.common.android.tasks.v2.notifications;

import static com.abiddarris.common.android.handlers.MainThreads.postDelayed;

import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.abiddarris.common.android.tasks.v2.IndeterminateProgress;
import com.abiddarris.common.android.tasks.v2.ProgressPublisher;
import com.abiddarris.common.utils.Exceptions;

abstract class NotificationProgressPublisher<P extends IndeterminateProgress> implements ProgressPublisher<P> {

    private final int id;
    private final NotificationCompat.Builder builder;
    private final NotificationManagerCompat notificationManager;

    private IndeterminateProgress progress;
    private Throwable throwable;

    NotificationProgressPublisher(NotificationCompat.Builder builder, Context context, int id) {
        this.notificationManager = NotificationManagerCompat.from(context);
        this.builder = builder;
        this.id = id;
    }

    @Override
    public void publish(P indeterminateProgress) {
        this.progress = indeterminateProgress;
        if (indeterminateProgress.getMessage() == null && indeterminateProgress.getTitle() == null) {
            return;
        }

        long maxProgress = getMaxProgress(indeterminateProgress);
        long progress = getProgress(indeterminateProgress);
        if (maxProgress > Integer.MAX_VALUE) {
            progress = Math.round(progress / ((float) maxProgress / Integer.MAX_VALUE));
        }

        builder.setContentTitle(indeterminateProgress.getTitle())
                .setContentText(indeterminateProgress.getMessage())
                .setProgress((int)maxProgress, (int) progress, maxProgress == 0);
        updateBigStyle();

        updateNotification();
    }

    @Override
    public void error(Throwable throwable) {
        this.throwable = throwable;
        updateBigStyle();
    }

    private void updateBigStyle() {
        if (throwable == null) {
            return;
        }
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(progress.getMessage() + "\n" + Exceptions.toString(throwable)));
    }

    @Override
    public void onFinish() {
        if (progress == null) {
            return;
        }

        builder.setContentTitle(progress.getTitle())
                .setContentText(progress.getMessage())
                .setProgress(0, 0, false);
        updateBigStyle();
        updateNotification();
    }

    protected abstract long getProgress(P progress);

    protected abstract long getMaxProgress(P progress);

    private void updateNotification() {
        notificationManager.notify(id, builder.build());
    }
}
