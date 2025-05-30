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

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code Logger} implementation that log to specified {@code OutputStream}
 *
 * @author Abiddarris
 * @since 1.1
 */
public class StreamLogger extends Logger {
    
    private OutputStream stream;
    
    public StreamLogger(Level level, String tag, OutputStream stream) {
        super(level, tag);
        
        checkNonNull(stream, "stream cannot be null!");
        
        this.stream = stream;
    }

    @Override
    protected void write(Level level, String log) throws IOException {
        stream.write((getTag() + " " + log + "\n").getBytes());
    }

}
