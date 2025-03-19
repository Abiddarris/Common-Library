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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * {@link RandomAccess} implementation that wraps {@link java.io.RandomAccessFile}.
 */
public class RandomAccessFile extends RandomAccess {

    private final java.io.RandomAccessFile raf;

    /**
     * Create a new {@link java.io.RandomAccessFile}
     *
     * @param file File to open
     * @param mode Open mode. See {@link java.io.RandomAccessFile} for more information
     * @throws FileNotFoundException If unable to open the file
     */
    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        this.raf = new java.io.RandomAccessFile(file, mode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readAt(long pos) throws IOException {
        ensureOpen();
        validatePos(pos);

        synchronized(this) {
            raf.seek(pos);
            return raf.read();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int readAt(byte[] b, long pos, int off, int len) throws IOException {
        ensureOpen();
        validateParams(b, pos, off, len);

        synchronized(this) {
            raf.seek(pos);
            return raf.read(b, off, len);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeAt(int b, long pos) throws IOException {
        ensureOpen();
        validatePos(pos);

        synchronized(this) {
            raf.seek(pos);
            raf.write(b);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeAt(byte[] b, long pos, int off, int len) throws IOException {
        ensureOpen();
        validateParams(b, pos, off, len);

        synchronized(this) {
            raf.seek(pos);
            raf.write(b, off, len);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLength() throws IOException {
        return raf.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLength(long newLength) throws IOException {
        ensureOpen();

        synchronized (this) {
            raf.setLength(newLength);
            if(pointer > newLength) {
                pointer = newLength;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        super.close();

        raf.close();
    }
}
