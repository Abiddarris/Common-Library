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
package com.abiddarris.common.logs;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import com.abiddarris.common.utils.Exceptions;

public abstract class Logger {

    private Level defaultLevel;
    private String tag;

    public Logger(Level defaultLevel, String tag) {
        checkNonNull(defaultLevel, "level cannot be null");
        checkNonNull(tag, "tag cannot be null");
        
        this.defaultLevel = defaultLevel;
        this.tag = tag;
    }

    public abstract void log(String string);

    public void log(Object obj) {
        log(defaultLevel, obj);
    }

    public void log(Level level, Object obj) {
        if (obj instanceof Throwable) {
            obj = Exceptions.toString((Throwable) obj);
        }
        log(obj == null ? "null" : obj.toString());
    }

    public Level getDefaultLevel() {
        return this.defaultLevel;
    }

    public void setDefaultLevel(Level defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public String getTag() {
        return this.tag;
    }
}
