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
import java.io.InputStream;

/**
 * Wrap {@code RandomAccess} as {@code InputStream}.
 *
 * @author Abiddarris
 */
public class RandomAccessInputStream extends InputStream {
    
    /**
     * Store associated {@code RandomAccess}
     */
    private final RandomAccess randomAccess;

    /**
     * Create new {@code RandomAccessInputStream} from specified {@code RandomAccess}
     *
     * @param randomAccess {@code RandomAccess} to wrap
     * @throws NullPointerException if {@code randomAccess} is {@code null}
     */
    public RandomAccessInputStream(RandomAccess randomAccess) {
        checkNonNull(randomAccess);
        
        this.randomAccess = randomAccess;
    }

    /**
     * Delegate {@code read()} call to {@link RandomAccess#read()}
     *
     * @return the next byte of data, or {@code -1} if the end of the
     *         {@code InputStream} has been reached.
     * @throws IOException if an I/O error occurs. Not thrown if end of
     *         {@code InputStream} has been reached.
     */
    @Override
    public int read() throws IOException {
        return randomAccess.read();
    }

    /**
     * Delegate {@code read(byte[])} call to {@link RandomAccess#read(byte[])}
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of this {@code InputStream} has been reached.          
     * @throws IOException If the first byte cannot be read for any reason other than end
     *         of {@code RandomAccess}, or if the {@code InputStream} has been closed, or 
     *         if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     */
    @Override
    public int read(byte[] b) throws IOException {
        return randomAccess.read(b);
    }

    /**
     * Delegate {@code read(byte[], int, int)} call to {@link RandomAccess#read(byte[], int, int)}
     *
     * @param b the buffer into which the data is read.
     * @param off the start offset in array {@code b} at which the data is written.           
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of the {@code InputStream} has been reached.    
     * @throws IOException If the first byte cannot be read for any reason other than end 
     *         of {@code InputStream}, or if the {@code InputStream} has been closed, 
     *         or if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, 
     *         or {@code len} is greater than {@code b.length - off}
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return randomAccess.read(b, off, len);
    }

    /**
     * Delegate {@code skip(long)} call to {@link RandomAccess#skipBytes(long)}
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long skip(long n) throws IOException {     
        return randomAccess.skipBytes(n);
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread. A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * <p>It is never correct to use the return value of this method to allocate
     * a buffer intended to hold all data in this stream.
     * @return an estimate of the number of bytes that can be read (or skipped
     *         over) from this input stream without blocking or {@code 0} when
     *         it reaches the end of the input stream.
     * @throws IOException if an I/O error occurs or stream closed.
     */
    @Override
    public int available() throws IOException {
        randomAccess.ensureOpen();
        
        return (int) Math.min(randomAccess.getLength() - randomAccess.getPointer(), Integer.MAX_VALUE);
    }

    /**
     * Delegate {@code #close()} call to {@link RandomAccess#close()}
     * 
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        randomAccess.close();
    }
    
}
