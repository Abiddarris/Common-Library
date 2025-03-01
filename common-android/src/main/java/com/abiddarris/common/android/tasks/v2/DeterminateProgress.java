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

import static com.abiddarris.common.utils.Preconditions.checkNonNegative;

public class DeterminateProgress extends IndeterminateProgress {

    private long maxProgress;
    private long progress;

    public long getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(long maxProgress) {
        checkNonNegative(progress, "maxProgress cannot be negative");

        this.maxProgress = maxProgress;
    }

    public void setProgress(long progress) {
        checkNonNegative(progress, "progress cannot be negative");

        this.progress = progress;
    }

    public long getProgress() {
        return progress;
    }
}
