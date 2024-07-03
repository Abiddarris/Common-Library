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
package com.abiddarris.common.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class that provides utilities for {@code InputStream}.
 *
 * @since 1.0
 * @author Abiddarris
 */
public final class InputStreams {
    
    /**
     * Prevent from being created
     */
    private InputStreams() {
    }
    
    /**
     * Reads {@code InputStream} till newline or the end of file
     * 
     * @param stream {@code InputStream} to read
     * @throws IOException If an error occurs while reading the stream
     * @return readed data in bytes of array
     */
    public static byte[] readLine(InputStream stream) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int byte0;
        while((byte0 = stream.read()) != -1 && byte0 != '\n') {
            outputStream.write(byte0);
        }
        
        return outputStream.toByteArray();
    }
}
