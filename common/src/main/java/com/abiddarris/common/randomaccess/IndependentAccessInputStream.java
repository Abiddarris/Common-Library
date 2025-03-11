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

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides independent reading of {@code RandomAccess}.
 *
 * The difference between <p>{@code RandomAccess.newInputStream()} and 
 * {@code RandomAccess.newIndependentInputStream()} is {@code IndependentInputStream} has its
 * own internal pointer. Meaning any call to {@code RandomAccess} methods like {@code read()} do not
 * affect {@code IndependentInputStream} (except call to {@code close()}). 
 *
 * <p>Regular {@code InputStream} only delegates the call into {@code RandomAccess} itself. 
 * Meaning any call to {@code InputStream} will affect {@code RandomAccess} and vice virca.
 *
 * @author Abiddarris
 */
public class IndependentAccessInputStream extends InputStream {
  
    /**
     * {@code RandomAccess} instance
     */
    private RandomAccess randomAccess;
    
    /**
     * Internal pointer
     */
    private long pointer = 0;

    /**
     * Create new {@code IndependentAccessInputStream} from specified
     * {@code RandomAccess}.
     *
     * @param randomAccess RandomAccess
     * @throws NullPointerException If {@code randomAccess} is {@code null}
     */
    public IndependentAccessInputStream(RandomAccess randomAccess) {
        this.randomAccess = randomAccess;
    }
    
    /**
     * Reads a byte of data from this {@code InputStream}. The byte is returned
     * as an integer in the range 0 to 255 {@code 0x00-0x0ff}. This method
     * blocks if no input is yet available.
     *
     * @return the next byte of data, or {@code -1} if the end of the
     *         {@code InputStream} has been reached.
     * @throws IOException if an I/O error occurs. Not thrown if end of
     *         {@code InputStream} has been reached.
     */
    @Override
    public synchronized int read() throws IOException {
        int b = randomAccess.readAt(pointer);     
        if(b != -1) pointer++;
        
        return b;
    }

    /**
     * Reads up to {@code len} bytes of data from this {@code InputStream} into an
     * array of bytes. This method blocks until at least one byte of input is available.
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
    public synchronized int read(byte[] b, int off, int len) throws IOException {
        len = randomAccess.readAt(b, pointer, off, len);
        if(len > 0) {
            pointer += len;
        }
        return len;
    }

    /**
     * Attempts to skip over {@code n} bytes of input discarding the skipped bytes.
     * 
     * <p>This method may skip over some smaller number of bytes, possibly zero.
     * This may result from any of a number of conditions; reaching end of
     * {@code InputStream} before {@code n} bytes have been skipped is only one
     * possibility. The actual number of bytes skipped is returned. If {@code n}
     * is negative, no bytes are skipped.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long skip(long n) throws IOException {
        randomAccess.ensureOpen();

        if(n <= 0) return 0;

        synchronized(this) {
            long length = randomAccess.getLength();
            long newPointer = pointer + n;
            if(newPointer > length) newPointer = length;     
            
            this.pointer = newPointer;
            return newPointer - pointer;
        }      
    }

    /**
     * Returns an estimate of the number of bytes that can be read (or
     * skipped over) from this input stream without blocking by the next
     * invocation of a method for this input stream. The next invocation
     * might be the same thread or another thread.  A single read or skip of this
     * many bytes will not block, but may read or skip fewer bytes.
     *
     * <p> Note that while some implementations of {@code InputStream} will return
     * the total number of bytes in the stream, many will not.  It is
     * never correct to use the return value of this method to allocate
     * a buffer intended to hold all data in this stream.
     * @return an estimate of the number of bytes that can be read (or skipped
     *         over) from this input stream without blocking or {@code 0} when
     *         it reaches the end of the input stream.
     * @throws IOException if an I/O error occurs or stream closed.
     */
    @Override
    public int available() throws IOException {
        randomAccess.ensureOpen();      
        
        synchronized(this) {         
            return (int) Math.min(randomAccess.getLength() - pointer, Integer.MAX_VALUE);
        }      
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @throws IOException  if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        randomAccess.close();
    }

}
