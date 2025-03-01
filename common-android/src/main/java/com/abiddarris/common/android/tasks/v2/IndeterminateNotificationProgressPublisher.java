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

import static com.abiddarris.common.utils.Randoms.randomInt;

import android.content.Context;

import androidx.core.app.NotificationCompat;

public class IndeterminateNotificationProgressPublisher extends NotificationProgressPublisher<IndeterminateProgress> {

    public IndeterminateNotificationProgressPublisher(NotificationCompat.Builder builder, Context context) {
        this(builder, context, randomInt(Integer.MAX_VALUE));
    }

    public IndeterminateNotificationProgressPublisher(NotificationCompat.Builder builder, Context context, int id) {
        super(builder, context, id);
    }

    @Override
    protected final long getProgress(IndeterminateProgress progress) {
        return 0;
    }

    @Override
    protected final long getMaxProgress(IndeterminateProgress progress) {
        return 0;
    }
}
