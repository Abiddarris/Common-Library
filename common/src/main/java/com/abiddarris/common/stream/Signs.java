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

/**
 * Class that provides utilities to convert signed to unsigned data type
 *
 * @author Abiddarris
 * @since 1.0
 */
public final class Signs {
    
    /**
     * Prevent this class from being created
     */
    private Signs() {}
    
    /**
     * Convert signed bytes to unsigned bytes
     *
     * @param b Array of bytes to convert to unsigned bytes
     * @return New array containing unsigned bytes
     * @since 1.0
     */
    public static int[] unsign(byte[] b) {
        int[] unsigned = new int[b.length];
        for(int i = 0; i < b.length; ++i) {
        	unsigned[i] = b[i] & 0xFF;
        }
        return unsigned;
    }
    
    /**
     * Convert unsigned bytes to signed bytes
     *
     * @param b Array of bytes to convert to signed bytes
     * @return New array containing signed bytes
     * @since 1.0
     */
    public static byte[] sign(int[] b) {
        byte[] signed = new byte[b.length];
        for(int i = 0; i < b.length; ++i) {
        	signed[i] = (byte)b[i];
        }
        return signed;
    }
    
}
