/***********************************************************************************
 * Copyright 2024 - 2025 Abiddarris
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

import java.util.concurrent.ThreadFactory;

/**
 * {@code ThreadFactory} implementation that creates daemon threads
 *
 * @author Abiddarris
 */
public class DaemonThreadFactory implements ThreadFactory {

    /**
     * Returns new daemon {@code Thread}
     *
     * @return Daemon {@code Thread}
     */
    @Override
    public Thread newThread(Runnable runnable) {       
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        
        return thread;
    }
    
}
