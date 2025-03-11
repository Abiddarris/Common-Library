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

import com.abiddarris.common.utils.Maths;

import java.io.IOException;
import java.util.Arrays;

/**
 * An implementation of {@code RandomAccess} that provides writing
 * and reading from memory
 *
 * <p>This class limit the bytes can be written into {@code 2^31} due to
 * array limitation.
 * 
 * @author Abiddarris
 */
public class RandomAccessMemory extends RandomAccess {
    
    /**
     * Hold growable array of bytes 
     */
    private byte[] data = new byte[1]; 
    
    /**
     * This {@code RandomAccessMemory}'s length
     */
    private int length;

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code pos} is less than {@code 0}, 
     *         or {@code pos} is larger than or equals {@code Integer.MAX_VALUE}
     */
    @Override
    public void seek(long pos) throws IOException {
        super.seek(pos);       
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code pos} is less than {@code 0},     
     *         or {@code pos} is larger than or equals {@code Integer.MAX_VALUE}
     */
    @Override
    public int readAt(long pos) throws IOException {
        ensureOpen();
        validatePos(pos);
        
        int position = (int)pos;
        
        synchronized(this) {      
            if(getPointer() > length) {
                growToIfNeeded(position);
            }
            if(pos >= length) return -1;

            
            return data[position];
        }      
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code pos} less than zero or 
     *         larger than or equals {@code Integer.MAX_VALUE}, {@code off} is negative, 
     *         {@code len} is negative, or {@code len} negative, or {@code len} 
     *         is greater than {@code b.length - off}
     */
    @Override
    public int readAt(byte[] b, long pos, int off, int len) throws IOException {
        ensureOpen();
        validateParams(b, pos, off, len);
        
        int position = (int)pos;
        
        if(len == 0) return 0;
        
        synchronized(this) {
            if(getPointer() > length) {
                growToIfNeeded(position);
            }
            if(position >= length) return -1;
            if(position + len >= length) len = length - position;

            System.arraycopy(data, position, b, off, len);          
        }     
        
        return len;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code pos} is less than {@code 0}, 
     *         or {@code pos} is larger than or equals {@code Integer.MAX_VALUE}
     */
    @Override
    public void writeAt(int b, long pos) throws IOException {
        ensureOpen();
        validatePos(pos);
        
        if(pos < 0) throw new IndexOutOfBoundsException();
              
        int position = (int)pos;
        
        synchronized(this) {
            growToIfNeeded(position);
            data[position] = (byte)b;
        }      
    }
    /**
    * {@inheritDoc}
    * 
    * @throws IndexOutOfBoundsException If {@code pos} is negative or greater than or equals
    *         {@code Integer.MAX_VALUE}, or {@code off} is negative, {@code len} is negative, 
    *         or {@code len} is greater than {@code b.length - off}
    */
    @Override
    public void writeAt(byte[] b, long pos, int off, int len) throws IOException  {
        ensureOpen();
        validateParams(b, pos, off, len);
        
        if(len == 0) return;
        
        int position = (int)pos;
        
        synchronized(this) {
            growToIfNeeded(position + len - 1);
            System.arraycopy(b, off, data, position, len);
        }       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLength() throws IOException {
        return length;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code newLength} is less than zero
     *         or greater than {@code Integer.MAX_VALUE}
     */
    @Override
    public void setLength(long newLength) throws IOException {     
        if(newLength < 0)
            throw new IndexOutOfBoundsException();
        if(newLength > Integer.MAX_VALUE)
            throw new IllegalArgumentException("newLength cannot be greater than Integer.MAX_VALUE");
            
        ensureOpen();
        
        int nLength = (int)newLength;
        synchronized(this) {          
            growToIfNeeded(nLength - 1);
            length = nLength;
            
            if(pointer > length) {
                pointer = length;
            }
        }       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        super.close();
        
        synchronized(this) {
            data = null;
        }       
    }

    /**
     * {@inheritDoc}
     *
     * @throws IndexOutOfBoundsException if {@code pos} less than zero,
     *         or greater than or equals {@code Integer.MAX_VALUE}
     */
    @Override
    protected void validatePos(long pos) {
        super.validatePos(pos);
        
        if (pos >= Integer.MAX_VALUE) {
            throw new IllegalArgumentException("pos cannot be greater than or equals Integer.MAX_VALUE");
        } 
    }
    
    /**
     * Grow to some size that making {@code data[pos]} not to throw
     * an exception
     *
     * @param pos Position that you want to pass to an array to prevent 
     *        {@code ArrayIndexOutOfBoundsException}.
     */
    private void growToIfNeeded(int pos) {
        if(pos < length) {
            return;
        }
        
        if(pos < data.length) {
            int newLength = pos + 1;          
            Arrays.fill(data, length, newLength, (byte)0);
            
            length = newLength;
            return;
        }
        
        length = pos + 1;      
        
        int power = (int) Maths.log(2, length);
        long newLength = (long) Math.pow(2, power);     
        newLength = newLength < length ? newLength * 2 : newLength;
        
        byte[] newData = new byte[newLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)newLength];   
        
        System.arraycopy(data, 0, newData, 0, data.length);
        
        data = newData;       
    }
}
