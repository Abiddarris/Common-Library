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

import static com.abiddarris.common.stream.InputStreams.readAll;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Utilities for compressing and decompressing
 *
 * @author Abiddarris
 * @since 1.0
 */
public final class Compresses {
    
    /**
     * Decompress array of bytes.
     *
     * @param data bytes to decompress.
     * @throws IOException If I/O error occurs.
     * @return Decompressed bytes.
     * @since 1.0
     */
    public static byte[] decompress(byte[] data) throws IOException {
        InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(data));
        
        return readAll(inflater);
    }
}
