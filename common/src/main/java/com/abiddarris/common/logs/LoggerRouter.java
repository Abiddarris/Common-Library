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
package com.abiddarris.common.logs;

import java.util.HashMap;
import java.util.Map;

public class LoggerRouter extends Logger {

    private final Map<Level, Logger> loggerMap = new HashMap<>();

    public LoggerRouter(Level defaultLevel, String tag) {
        super(defaultLevel, tag);
    }

    public void addRoute(Level level, Logger logger) {
        loggerMap.put(level, logger);
    }

    public Logger removeRoute(Level level) {
        return loggerMap.remove(level);
    }

    @Override
    public void log(Level level, Object obj) {
        ensureOpen();

        if (level == null) {
            level = getDefaultLevel();
        }
        if (level.getLevel() < getMinLoggedLevel().getLevel()) {
            return;
        }

        Logger logger = loggerMap.get(level);
        if (logger == null) {
            super.log(level, obj);
            return;
        }

        logger.log(level, obj);
    }
}