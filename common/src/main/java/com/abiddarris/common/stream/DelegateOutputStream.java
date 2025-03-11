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

import java.io.IOException;
import java.io.OutputStream;

/**
 * Class that wraps existing {@code OutputStream} and delegate this class' methods call
 * to specified {@code OutputStream}.
 *
 * <p>This is useful if you want to manipulate variables and delegate
 * it to existing {@code OutputStream}
 *
 * @author Abiddarris
 * @since 1.0
 */
public class DelegateOutputStream extends OutputStream {
    
    /**
     * Delegate destination
     */
    private OutputStream stream;

    /**
     * Create new {@code DelegateOutputStream} from specified stream
     *
     * @param stream An existing stream
     * @throws NullPointerException If {@code stream} is {@code null}
     * @since 1.0
     */
    public DelegateOutputStream(OutputStream stream) {
        checkNonNull(stream);
        
        this.stream = stream;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *         an <code>IOException</code> may be thrown if the
     *         output stream has been closed.
     * @since 1.0
     */
    @Override
    public void write(int b) throws IOException {
        stream.write(b);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * The general contract for <code>write(b, off, len)</code> is that
     * some of the bytes in the array <code>b</code> are written to the
     * output stream in order; element <code>b[off]</code> is the first
     * byte written and <code>b[off+len-1]</code> is the last byte written
     * by this operation.
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
     * @since 1.0
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }

    /**
     * Flushes this output stream and forces any buffered output bytes
     * to be written out. The general contract of <code>flush</code> is
     * that calling it is an indication that, if any bytes previously
     * written have been buffered by the implementation of the output
     * stream, such bytes should immediately be written to their
     * intended destination.
     * <p>
     * If the intended destination of this stream is an abstraction provided by
     * the underlying operating system, for example a file, then flushing the
     * stream guarantees only that bytes previously written to the stream are
     * passed to the operating system for writing; it does not guarantee that
     * they are actually written to a physical device such as a disk drive.
     *
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    @Override
    public void flush() throws IOException {
        stream.flush();
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream. The general contract of <code>close</code>
     * is that it closes the output stream. A closed stream cannot perform
     * output operations and cannot be reopened.
     *
     *
     * @throws IOException if an I/O error occurs.
     * @since 1.0
     */
    @Override
    public void close() throws IOException {
        stream.close();
    }
    
}
