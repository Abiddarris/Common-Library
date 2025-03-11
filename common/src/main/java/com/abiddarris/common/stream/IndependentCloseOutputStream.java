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
package com.abiddarris.common.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * {@code OutputStream} implementation that does not close associated {@code OutputStream}
 * when {@link #close()} called
 *
 * <p>When {@link #close()} has been called, only this stream is closed. For example any call
 * to {@link #write(int)} is throwing {@code IOException} if {@link #close()} has been called.
 * but this class does not close {@code OutputStream} that passed to constructor. Meaning any call
 * to {@code OutputStream} does not throw an {@code Exception} even though this stream already closed.
 * unless there is a call to {@code close()} to {@code OutputStream}
 *
 * @author Abiddarris
 */
public class IndependentCloseOutputStream extends DelegateOutputStream {
    
    /**
     * Hold this class state
     */
    private CloseableObject closeObject = new CloseableObject();
    
    /**
     * Create new {@code IndependentCloseOutputStream} from specified stream
     *
     * @param stream An existing stream
     * @throws NullPointerException If {@code stream} is {@code null}
     */
    public IndependentCloseOutputStream(OutputStream stream) {
        super(stream);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(int b) throws IOException {
        closeObject.ensureOpen();
        
        super.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b) throws IOException {
        closeObject.ensureOpen();
        
        super.write(b);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        closeObject.ensureOpen();
        
        super.write(b, off, len);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flush() throws IOException {
        closeObject.ensureOpen();
        
        super.flush();
    }

    /**
     * Close this {@code IndependentCloseOutputStream} without closing passed
     * {@code tCloseInputStream}.
     */
    @Override
    public void close() {
        closeObject.close();            
    }
    
}
