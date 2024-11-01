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
package com.abiddarris.common.utils;

import java.nio.ByteOrder;

public class Numbers {

    public static Object decode(byte[] bytes, ByteOrder order, boolean signed) {
        if (bytes.length > 8) {
            throw new UnsupportedOperationException("I dont support that yet.");
        }
        if (order != ByteOrder.LITTLE_ENDIAN) {
            throw new UnsupportedOperationException("I dont support that yet.");
        }
        if (!signed) {
            throw new UnsupportedOperationException("I dont support that yet.");
        }
        
        long result = 0;

        for (int i = 0; i < bytes.length; i++) {
            result |= bytes[i] << i * 8;
        }

        return result;
    }

}
