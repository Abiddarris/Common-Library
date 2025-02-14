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

import com.abiddarris.common.stream.CloseableObject;
import com.abiddarris.common.utils.Exceptions;

import java.io.Closeable;
import java.io.IOException;

public abstract class Logger implements Closeable {

    private CloseableObject state = new CloseableObject();
    private Level defaultLevel;
    private LogFormatter logFormatter = new DefaultLogFormatter();
    private Level minLoggedLevel;
    private String tag;

    public Logger(Level defaultLevel, String tag) {
        checkNonNull(defaultLevel, "level cannot be null");
        checkNonNull(tag, "tag cannot be null");
        
        this.defaultLevel = defaultLevel;
        this.tag = tag;
    }

    public void log(String string) {}

    public void log(Object obj) {
        log(defaultLevel, obj);
    }

    public void log(Level level, Object obj) {
        try {
            state.ensureOpen();
        } catch (IOException e) {
            throw new LogException("Log closed");
        }

        if (level.getLevel() < getMinLoggedLevel().getLevel()) {
            return;
        }

        if (obj instanceof Throwable) {
            obj = Exceptions.toString((Throwable) obj);
        }
        safeWrite(level, obj == null ? "null" : obj.toString());
    }

    public Level getDefaultLevel() {
        return this.defaultLevel;
    }

    public void setDefaultLevel(Level defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public void setTag(String tag) {
        checkNonNull(tag, "tag cannot be null");

        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }

    public LogFormatter getLogFormatter() {
        return logFormatter;
    }

    public void setLogFormatter(LogFormatter logFormatter) {
        checkNonNull(logFormatter, "logFormatter cannot be null");

        this.logFormatter = logFormatter;
    }

    public Logger deriveLogger(String tag) {
        return new DelegateLogger(this, defaultLevel, tag);
    }

    public void flush() {
    }

    @Override
    public void close() throws IOException {
        state.close();
    }

    protected void write(Level level, String log) throws IOException {
        log(log);
    }

    private void safeWrite(Level level, String log) {
        try {
            write(level, log);
        } catch (IOException e) {
            throw new LogException("Failed to log", e);
        }
    }

    public static String getTag(Object obj) {
        checkNonNull(obj, "Require non null obj");
        if (!(obj instanceof Class)) {
            obj = obj.getClass();
        }

        Class<?> clazz = (Class<?>)obj;
        return clazz.getName();
    }

    public Level getMinLoggedLevel() {
        return minLoggedLevel;
    }

    public void setMinLoggedLevel(Level minLoggedLevel) {
        checkNonNull(minLoggedLevel, "minLoggedLevel cannot be null");

        this.minLoggedLevel = minLoggedLevel;
    }
}
