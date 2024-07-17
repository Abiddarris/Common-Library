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

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

/**
 * Provide methods for validating parameters for streams.
 *
 * @since 1.0
 * @author Abiddarris
 */
public final class StreamPrecondition {
    
    /**
     * Prevent this class from being instantiated
     */
    private StreamPrecondition() {
    }
    
    /**
     * Validate stream parameter
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array {@code b} at which the data is written.           
     * @param len the maximum number of bytes read.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, 
     *         or {@code len} is greater than {@code b.length - off}                   
     * @since 1.0
     */
    public static void validateParams(byte[] b, int off, int len) {
        checkNonNull(b);
        
        if(off < 0 || len < 0 || len + off > b.length) {
            throw new IndexOutOfBoundsException();
        }
    }
}
