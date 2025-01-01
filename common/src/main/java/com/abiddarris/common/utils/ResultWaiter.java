/***********************************************************************************
 * Copyright 2025 Abiddarris
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

public class ResultWaiter<R> {

    private final Object lock = new Object();

    private boolean resultAvailable;
    private R result;

    public R getResult() throws InterruptedException {
        synchronized (lock) {
            while (!resultAvailable) {
                lock.wait();
            }

            return result;
        }
    }

    public void sendResult(R result) {
        synchronized (lock) {
            this.result = result;

            resultAvailable = true;
            lock.notifyAll();
        }
    }

}
