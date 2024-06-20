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
package com.abiddarris.common.utils;

public interface BaseRunnable extends Runnable {
    
    public abstract void execute() throws Exception;
    
    @Override
    public default void run() {
        try {
            execute();
        } catch (Exception e) {
            onThrowableCatched(e);
            onExceptionThrown(e);
        } catch (Throwable e) {
            onThrowableCatched(e);
        } finally {
            onFinally();
        }
    }
    
    public default void onFinally() {}
    
    @Deprecated
    public default void onExceptionThrown(Exception e) {
        e.printStackTrace();
    }
    
    public default void onThrowableCatched(Throwable throwable) {
        throwable.printStackTrace();
    }
}
