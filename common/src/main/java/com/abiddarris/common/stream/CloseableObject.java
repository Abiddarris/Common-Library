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

import java.io.Closeable;
import java.io.IOException;

/**
 * A class that provides {@code Closeable} mechanism for implementing
 * {@code Closeable} class.
 *
 * <p>This method simplify closing an object and throwing an {@code IOException}
 * if {@link #close()} method already called by calling {@link #ensureOpen()}.
 * This class usually used by {@code Closeable} classes.
 *
 * @author Abiddarris
 * @since 1.0
 */
public class CloseableObject implements Closeable { 

    /**
     * Hold the state of this {@code CloseableObject}
     */
    private boolean closed;
    
    /**
     * Close this {@code CloseableObject}
     *
     * @since 1.0
     */
    public void close() {
        closed = true;
    }
    
    /**
     * Returns {@code true} if {@link #close()} has not been called yet.
     *
     * @return {@code true} if {@link #close()} has not been called yet.
     *         Otherwise returns {@code false}
     * @since 1.0
     */
    public boolean isOpen() {
        return !closed;
    }
    
    /**
     * Throws an {@code IOException} if {@link #close()} had been called
     *
     * @throws IOException if {@link #close()} had been called
     * @since 1.0
     */
    public void ensureOpen() throws IOException {
        if(!isOpen()) {
            throw new IOException("Closed");
        }
    }
    
}