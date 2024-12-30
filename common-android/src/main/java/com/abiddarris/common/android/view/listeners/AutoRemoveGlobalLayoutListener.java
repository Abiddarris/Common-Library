/***********************************************************************************
 * Copyright 2024 Abiddarris
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
package com.abiddarris.common.android.view.listeners;

import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.abiddarris.common.utils.Preconditions;

public class AutoRemoveGlobalLayoutListener implements OnGlobalLayoutListener {

    private ViewTreeObserver observer;
    private OnGlobalLayoutListener listener;

    public AutoRemoveGlobalLayoutListener(
            ViewTreeObserver observer, OnGlobalLayoutListener listener) {
        Preconditions.checkNonNull(observer, "observer cannot be null");
        
        this.observer = observer;
        this.listener = listener;
        
        observer.addOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {
        observer.removeOnGlobalLayoutListener(this);
        
        if(listener != null)
            listener.onGlobalLayout();
    }
}
