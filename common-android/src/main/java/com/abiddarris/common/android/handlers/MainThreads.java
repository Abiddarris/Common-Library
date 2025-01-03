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
package com.abiddarris.common.android.handlers;

import static android.os.Looper.getMainLooper;
import static android.os.Looper.myLooper;

import android.os.Handler;
import android.os.Looper;

public final class MainThreads {
    
    private static Handler handler;
    
    private MainThreads() {
    }

    public static boolean isMainThread() {
        return myLooper() == getMainLooper();
    }
    
    public static boolean post(Runnable runnable) {
        return getInstance().post(runnable);
    }
    
    public static boolean postDelayed(Runnable runnable, long delay) {
        return getInstance().postDelayed(runnable, delay);
    }
    
    public static void removeCallbacks(Runnable runnable) {
        getInstance().removeCallbacks(runnable);
    }
    
    private static Handler getInstance() {
        if(handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }
    
}
