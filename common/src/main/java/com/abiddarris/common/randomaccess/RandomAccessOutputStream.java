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
package com.abiddarris.common.randomaccess;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrap {@code RandomAccess} as {@code OutputStream}.
 *
 * @author Abiddarris
 */
public class RandomAccessOutputStream extends OutputStream {
    
    /**
     * Store associated {@code RandomAccess}
     */
    private final RandomAccess randomAccess;

    /**
     * Create new {@code RandomAccessOutputStream} from specified {@code RandomAccess}
     *
     * @param randomAccess {@code RandomAccess} to wrap
     * @throws NullPointerException if {@code randomAccess} is {@code null}
     */
    public RandomAccessOutputStream(RandomAccess randomAccess) {
        checkNonNull(randomAccess);
        
        this.randomAccess = randomAccess;
    }

    /**
     * Delegate {@code write(int)} call to {@link RandomAccess#write(int)}
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *         an <code>IOException</code> may be thrown if the
     *         output stream has been closed.
     */
    @Override
    public void write(int b) throws IOException {
        randomAccess.write(b);
    }

    /**
     * Delegate {@code write(byte[])} call to {@link RandomAccess#write(byte[])}
     *
     * @param b the data.
     * @throws NullPointerException if {@code b} is {@code null}
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void write(byte[] b) throws IOException {
        randomAccess.write(b);
    }

    /**
     * Delegate {@code write(byte[], int, int)} call to {@link RandomAccess#write(byte[], int, int)}
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws NullPointerException If <code>b</code> is <code>null</code>
     * @throws IndexOutOfBoundsException If <code>off</code> is negative, or <code>len</code> 
     *         is negative, or <code>off+len</code> is greater than the length of the array
     *         <code>b</code>
     * @throws IOException if an I/O error occurs. In particular,
     *          an <code>IOException</code> is thrown if the output
     *          stream is closed.
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        randomAccess.write(b, off, len);
    }

    /**
     * Delegate {@link #close()} call to {@link RandomAccess#close()}
     * 
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        randomAccess.close();
    }
    
}
