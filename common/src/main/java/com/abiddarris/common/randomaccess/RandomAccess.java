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

import com.abiddarris.common.stream.CloseableObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstract class that define contract for Random Access
 *
 * @author Abiddarris
 * @since 1.1
 */
public abstract class RandomAccess implements Closeable {
    
    /**
     * Store state of this {@code RandomAccess}. 
     */
    private final CloseableObject closeObject = new CloseableObject();
    
    /**
     * Hold {@code InputStream} instance that wrap this {@code RandomAccess}
     */
    private RandomAccessInputStream inputStream;
    
    /**
     * Hold {@code OutputStream} instance that wrap this {@code RandomAccess}
     */
    private RandomAccessOutputStream outputStream;
    
    /**
     * Stores a pointer
     *
     * <p>{@code pointer} is used by {@link #read() #write()} method.
     * pointer defines the offset when calling {@link #read()} or {@link #write(int)}
     */
    protected long pointer;
    
    /**
     * Reads a byte of data from this {@code RandomAccess} at specified position.
     * The byte is returned as an integer in the range 0 to 255 {@code 0x00-0x0ff}. 
     * This method blocks if no input is yet available.
     * 
     * @param pos Position to read.
     * @return the byte in the specified position, or {@code -1} if the 
     *         end of the {@code RandomAccess} has been reached.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero
     * @throws IOException if an I/O error occurs. Not thrown if end of
     *                     {@code RandomAccess} has been reached.
     */
    public abstract int readAt(long pos) throws IOException;
    
    /**
     * Reads up to {@code len} bytes of data from this {@code RandomAccess} into an
     * array of bytes from the specified position. This method blocks until at least 
     * one byte of input is available.
     * 
     * @param b the buffer into which the data is read.
     * @param pos Starting position
     * @param off the start offset in array {@code b} at which the data is written.           
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of the {@code RandomAccess} has been reached.    
     * @throws IOException If the first byte cannot be read for any reason other than end 
     *         of {@code RandomAccess}, or if the {@code RandomAccess} has been closed, 
     *         or if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero, {@code off} is negative,
     *         {@code len} is negative, or {@code len} negative, or {@code len} is greater than
     *         {@code b.length - off}
     */
    public abstract int readAt(byte[] b, long pos, int off, int len) throws IOException;
    
    /**
     * Writes the specified byte to this {@code RandomAccess} in the specified position.
     *
     * @param b the {@code byte} to be written.
     * @param pos Specified position
     * @throws IOException if an I/O error occurs.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero
     */
    public abstract void writeAt(int b, long pos) throws IOException;
    
    /**
     * Writes {@code len} bytes from the specified byte array starting at 
     * offset {@code off} to this {@code RandomAccess} in the spefified position.
     *
     * @param b the data.
     * @param pos Starting position
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException  if an I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero, {@code off} is negative, 
     *         {@code len} is negative, or {@code len} is greater than {@code b.length - off}
     */
    public abstract void writeAt(byte[] b, long pos, int off, int len) throws IOException;
    
    /**
     * Returns the length of this {@code RandomAccess}.
     *
     * @return the length of this {@code RandomAccess}, measured in bytes.
     * @throws IOException if an I/O error occurs.
     */
    public abstract long getLength() throws IOException;
    
    /**
     * Sets the length of this {@code RandomAccess}.
     *
     * <p>If the present length of the {@code RandomAccess} as returned by the
     * {@code getLength} method is greater than the {@code newLength} argument
     * then the {@code RandomAccess} will be truncated.  In this case, if 
     * the offset as returned by the {@code getPointer} method is greater 
     * than {@code newLength} then after this method returns the offset will
     * be equal to {@code newLength}
     *
     * <p>If the present length of the {@code RandomAccess} as returned by the 
     * {@code getLength} method is smaller than the {@code newLength} argument 
     * then the {@code RandomAccess} will be extended. In this case, the contents 
     * of the extended portion of the {@code RandomAccess} are not defined.
     * 
     * @param newLength The desired length of the {@code RandomAccess}
     * @throws IndexOutOfBoundsException if {@code newLength} is less than zero
     * @throws IOException If an I/O error occurs
     */
    public abstract void setLength(long newLength) throws IOException;
    
    /**
     * Reads up to {@code b.length} bytes of data from this {@code RandomAccess}
     * into an array of bytes from specified {@code pos}. This method blocks until
     * at least one byte of input is available.
     *   
     * @param b the buffer into which the data is read.
     * @param pos Starting position
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of this {@code RandomAccess} has been reached.          
     * @throws IOException If the first byte cannot be read for any reason other than end
     *         of {@code RandomAccess}, or if the {@code RandomAccess} has been closed, or 
     *         if some other I/O error occurs.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero
     * @throws NullPointerException If {@code b} is {@code null}.
     */
    public int readAt(byte[] b, long pos) throws IOException {
        return readAt(b, pos, 0, b.length);
    }

    /**
     * Writes {@code b.length} bytes from the specified byte array to this {@code RandomAccess},
     * starting from the specified position.
     *
     * @param b the data.
     * @param pos Starting position
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException if {@code pos} less than zero
     */
    public void writeAt(byte[] b, long pos) throws IOException {
        writeAt(b, pos, 0, b.length);
    }

    /**
     * Reads a byte of data from this {@code RandomAccess}. The byte is returned
     * as an integer in the range 0 to 255 {@code 0x00-0x0ff}. This method
     * blocks if no input is yet available.
     *
     * @return the next byte of data, or {@code -1} if the end of the
     *         {@code RandomAccess} has been reached.
     * @throws IOException if an I/O error occurs. Not thrown if end of
     *         {@code RandomAccess} has been reached.
     */
    public int read() throws IOException {
        int b = readAt(pointer);

        if(b != -1) {
            synchronized(this) {
                pointer++;
            }
        }      
        
        return b;
    }
    
    /**
     * Reads up to {@code b.length} bytes of data from this {@code RandomAccess}
     * into an array of bytes. This method blocks until at least one byte of input
     * is available.
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of this {@code RandomAccess} has been reached.          
     * @throws IOException If the first byte cannot be read for any reason other than end
     *         of {@code RandomAccess}, or if the {@code RandomAccess} has been closed, or 
     *         if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Reads up to {@code len} bytes of data from this {@code RandomAccess} into an
     * array of bytes. This method blocks until at least one byte of input is available.
     * 
     * @param b the buffer into which the data is read.
     * @param off the start offset in array {@code b} at which the data is written.           
     * @param len the maximum number of bytes read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of the {@code RandomAccess} has been reached.    
     * @throws IOException If the first byte cannot be read for any reason other than end 
     *         of  {@code RandomAccess}, or if the {@code RandomAccess} has been closed, 
     *         or if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, 
     *         or {@code len} is greater than {@code b.length - off}
     */
    public int read(byte[] b, int off, int len) throws IOException {
        len = readAt(b, pointer, off, len);

        if(len != -1) {
            synchronized(this) {
                pointer += len;
            }
        }      
        
        return len;
    }

    /**
     * Attempts to skip over {@code n} bytes of input discarding the skipped bytes.
     * 
     * <p>This method may skip over some smaller number of bytes, possibly zero.
     * This may result from any of a number of conditions; reaching end of
     * {@code RandomAccess} before {@code n} bytes have been skipped is only one
     * possibility. The actual number of bytes skipped is returned. If {@code n}
     * is negative, no bytes are skipped.
     *
     * @param n the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException if an I/O error occurs.
     */
    public long skipBytes(long n) throws IOException {
        ensureOpen();
        
        if(n <= 0) return 0;
        
        long newPointer;
        long pointer;
        
        synchronized(this) {
            pointer = getPointer();
            long length = getLength();
            newPointer = pointer + n;
            if(newPointer > length) newPointer = length;         
        }
        
        seek(newPointer);
        
        return newPointer - pointer;
    }
    
    /**
     * Writes the specified byte to this {@code RandomAccess}. The write starts at
     * the current pointer.
     *
     * @param b the {@code byte} to be written.
     * @throws IOException if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        writeAt(b, pointer);
        
        synchronized(this) {
            pointer++;
        }
    }
   
    /**
     * Writes {@code b.length} bytes from the specified byte array
     * to this {@code RandomAccess}, starting at the current pointer.
     *
     * @param b the data.
     * @throws IOException if an I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     */
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes {@code len} bytes from the specified byte array
     * starting at offset {@code off} to this {@code RandomAccess}.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @throws IOException  if an I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, 
     *         or {@code len} is greater than {@code b.length - off}
     */
    public void write(byte[] b, int off, int len) throws IOException {
        writeAt(b, pointer, off, len);
        
        synchronized(this) {
            pointer += len;
        }      
    } 
    
    /**
     * Returns the current offset in this {@code RandomAccess}.
     *
     * @return the offset from the beginning of the {@code RandomAccess}, in bytes,
     *         at which the next read or write occurs.
     * @throws IOException if an I/O error occurs.
     */
    public long getPointer() throws IOException {
        return pointer;
    }
    
    /**
     * Sets the pointer offset, measured from the beginning of this
     * {@code RandomAccess}, at which the next read or write occurs. 
     * The offset may be set beyond the end of the {@code RandomAccess}.
     * Setting the offset beyond the end of the {@code RandomAccess}
     * does not change the {@code RandomAccess} length. The {@code RandomAccess} 
     * length will change only by writing after the offset has been set 
     * beyond the end of the {@code RandomAccess}. 
     *
     * @param pos the offset position, measured in bytes from the beginning
     *            of the {@code RandomAccess}, at which to set the pointer.         
     * @throws IndexOutOfBoundsException if {@code pos} is less than {@code 0} 
     * @throws IOException or if an I/O error occurs.
     */
    public synchronized void seek(long pos) throws IOException {
        ensureOpen();
        validatePos(pos);
        
        pointer = pos;
    } 
    
    /**
     * Returns {@code InputStream} representation of this {@code RandomAccess}
     *
     * <p>This method always returns the same instance of {@code InputStream} no matter
     * how many times it got called. Closing returned {@code InputStream} will close
     * this {@code RandomAccess}.
     *
     * <p>Invoking {@code read()} on returned {@code InputStream} will move this {@code RandomAccess}
     * pointer. Use {@link #newIndependentInputStream()} for reading without moving this {@code RandomAccsss}
     * pointer.
     *
     * @return {@code InputStream} representation of this {@code RandomAccess}
     * @throws IOException if I/O error occurs
     */
    public InputStream newInputStream() throws IOException {
        synchronized(this) {
            if(inputStream == null) {
                inputStream = new RandomAccessInputStream(this);
            }
        }
        return inputStream;
    }
    
    /**
     * Returns {@code OutputStream} representation of this {@code RandomAccess}
     *
     * <p>This method always returns the same instance of {@code OutputStream} no matter
     * how many times it got called. Closing returned {@code OutputStream} will close
     * this {@code RandomAccess}.
     *
     * <p>Invoking {@code write()} on returned {@code OutputStream} will move this {@code RandomAccess}
     * pointer. 
     *
     * @return {@code OutputStream} representation of this {@code RandomAccess}
     * @throws IOException if I/O error occurs
     */
    public OutputStream newOutputStream() throws IOException {
        synchronized(this) {
            if(outputStream == null) {
                outputStream = new RandomAccessOutputStream(this);
            }
        }       
        return outputStream;
    }
    
    /**
     * Returns {@code InputStream} representation of this {@code RandomAccess}
     *
     * <p>Invoking {@code read()} from {@code InputStream} that returned from this method does not
     * moving this {@code RandomAccess}'s pointer. This is the best solution for reading this 
     * {@code RandomAccess} concurrently.
     *
     * <p>Closing returned {@code InputStream} will close this {@code RandomAccess}.
     *
     * @return {@code InputStream} representation of this {@code RandomAccess}
     * @throws IOException if I/O error occurs
     */
    public InputStream newIndependentInputStream() throws IOException {
        return new IndependentAccessInputStream(this);
    }
    
    /**
     * Returns {@code OutputStream} representation of this {@code RandomAccess}
     *
     * <p>Invoking {@code write()} from {@code OutputStream} that returned from this method does not
     * moving this {@code RandomAccess}'s pointer. This is the best solution for reading this 
     * {@code RandomAccess} concurrently.
     *
     * <p>Closing returned {@code OutputStream} will close this {@code RandomAccess}.
     *
     * @return {@code OutputStream} representation of this {@code RandomAccess}
     * @throws IOException if I/O error occurs
     */
    public OutputStream newIndependentOutputStream() throws IOException {
    	return new IndependentAccessOutputStream(this);
    }
    
    /**
     * Close this {@code RandomAccess}
     *
     * <p>Subclasses of this class that ovveride this method must calls
     * its super implementation. Failure to do so would results in bugs
     * where {@code RandomAccess} is interactable even after call to {@link #close()}
     * 
     * @throws IOException if an I/O error occurs when closing this {@code RandomAccess}
     */
    public void close() throws IOException {
        closeObject.close();
    }   
    
    /**
     * Returns {@code true} if this {@code RandomAccess} is open
     *
     * @return {@code true} if this {@code RandomAccess} is open, otherwise
               returns {@code false}
     */
    public boolean isOpen() {
        return closeObject.isOpen();
    }
    
    /**
     * Ensure this {@code RandomAccess} is not closed yet.
     *
     * <p>This method can be called by subclass for some method that need
     * the {@code RandomAccess} to be open
     *
     * @throws IOException if this {@code RandomAccess} closed
     */
    protected void ensureOpen() throws IOException {
        closeObject.ensureOpen();
    }

    /**
     * Utility that invoked by this class or subclasses to 
     * validate the {@code pos}.
     *
     * <p>Subclasses may invoke this method to check if the position
     * is valid. Also subclasses may override this method to add more
     * validation to it.
     *
     * @param pos Position to validate
     * @throws IndexOutOfBoundsException if {@code pos} less than zero
     */
    protected void validatePos(long pos) {
        if(pos < 0) 
            throw new IndexOutOfBoundsException("pos cannot be less than zero");                
    }
    
    /**
     * Utility to validate common params in this class
     * 
     * <p>Subclasses may invoke this method to check if the position
     * is valid. Also subclasses may override this method to add more
     * validation to it.
     *
     * @param b Array to validate
     * @param off Offset to validate
     * @param len Length to validate
     * @throws NullPointerException if {@code b} is {@code null}
     * @throws IndexOutOfBoundsException If {@code off} is negative, {@code len} is negative, 
     *         or {@code len} negative, or {@code len} is greater than {@code b.length - off}
     */
    protected void validateParams(byte[] b, int off, int len) {
        checkNonNull(b);

        if (off < 0 || len < 0 || b.length - off < len) {
            throw new IndexOutOfBoundsException();
        }
    }
    
    /**
     * Utility to check common params in this class
     *
     * <p>Subclasses may invoke this method to check if the position
     * is valid. Also subclasses may override this method to add more
     * validation to it
     *
     * <p>By default this method will call {@link #validateParams(byte[], int, int)} 
     * and {@link #validatePos(long)} to validate
     *
     * @param b Array to validate
     * @param pos Position to validate   
     * @param off Offset to validate
     * @param len Length to validate
     * @throws NullPointerException if {@code b} is {@code null}
     * @throws IndexOutOfBoundsException if {@code pos} less than zero, {@code off} is negative, 
     *         {@code len} is negative, or {@code len} is greater than {@code b.length - off}
     */
    protected void validateParams(byte[] b, long pos, int off, int len) {
        validateParams(b, off, len);
        validatePos(pos);
    }
    
    
}
