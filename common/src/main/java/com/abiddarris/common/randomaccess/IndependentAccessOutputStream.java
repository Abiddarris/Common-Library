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
 * Provides independent write of {@code RandomAccess}.
 *
 * The difference between <p>{@code RandomAccess.newOutputStream()} and 
 * {@code RandomAccess.newIndependentOutputStream()} is {@code IndependentOutputStream} has its
 * own internal pointer. Meaning any call to {@code RandomAccess} methods like {@code write()} do not
 * affect {@code IndependentOutputStream} (except call to {@code close()}). 
 *
 * <p>Regular {@code OutputStream} only delegates the call into {@code RandomAccess} itself. 
 * Meaning any call to {@code OutputStream} will affect {@code RandomAccess} and vice virca.
 *
 * @author Abiddarris
 */
public class IndependentAccessOutputStream extends OutputStream {
    
    /**
     * {@code RandomAccess} instance
     */
    private final RandomAccess randomAccess;
    
    /**
     * Internal pointer
     */
    private long pointer = 0;

    /**
     * Create new {@code IndependentAccessOutputStream} from specified
     * {@code RandomAccess}.
     *
     * @param randomAccess RandomAccess
     * @throws NullPointerException If {@code randomAccess} is {@code null}
     */
    public IndependentAccessOutputStream(RandomAccess randomAccess) {
        checkNonNull(randomAccess);

        this.randomAccess = randomAccess;
    }

    /**
     * Delegate call to {@link RandomAccess#writeAt(int,long)}.
     *
     * @param b {@code byte} to write
     * @throws IOException if an I/O error occurs. In particular,
     *         an <code>IOException</code> may be thrown if the
     *         output stream has been closed.
     */
    @Override
    public synchronized void write(int b) throws IOException {
        randomAccess.writeAt(b, pointer);
        pointer++;
    }

    /**
     * Delegate call to {@link RandomAccess#writeAt(byte[], long, int, int)}.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws NullPointerException If <code>b</code> is <code>null</code>
     * @throws IndexOutOfBoundsException If <code>off</code> is negative, or <code>len</code> 
     *         is negative, or <code>off+len</code> is greater than the length of the array
     *         <code>b</code>
     * @throws IOException  if an I/O error occurs. In particular,
     *         an <code>IOException</code> is thrown if the output
     *         stream is closed.
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        randomAccess.writeAt(b, pointer, off, len);
        pointer += len;
    }

    /**
     * Closes this output stream and releases any system resources associated
     * with the stream.
     *
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        randomAccess.close();
    }
}
