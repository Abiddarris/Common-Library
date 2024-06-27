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

public final class Logs {
    
    private static LogFactory factory = new DefaultLogFactory();
    
    public static Logger newLogger(Level level, Object tag) {
        return newLogger(level, tag.getClass());
    }
    
    public static Logger newLogger(Level level, Class<?> clazz) {
        return newLogger(level, clazz.getName());
    }
    
    public static Logger newLogger(Level level, String tag) {
        return factory.newLogger(level, tag);
    }
}
