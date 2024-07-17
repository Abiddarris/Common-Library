package com.abiddarris.common.stream;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class that wraps existing {@code InputStream} and delegate this class' methods call
 * to specified {@code InputStream}.
 *
 * <p>This is useful if you want to manipulate variables and delegate
 * it to existing {@code InputStream}
 *
 * @author Abiddarris
 * @since 1.0
 */
public class DelegateInputStream extends InputStream {
    
    /**
     * Delegate destination
     */
    private InputStream stream;

    /**
     * Create new {@code DelegateInputStream} from specified stream
     *
     * @param stream An existing stream
     * @throws NullPointerException If {@code stream} is {@code null}
     * @since 1.0
     */
    public DelegateInputStream(InputStream stream) {
        checkNonNull(stream);
        
        this.stream = stream;
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
     * @since 1.0
     */
    @Override
    public int read() throws IOException {
        return stream.read();
    }

    /**
     * Reads up to {@code b.length} bytes of data from this {@code InputStream}
     * into an array of bytes. This method blocks until at least one byte of input
     * is available.
     *
     * @param b the buffer into which the data is read.
     * @return the total number of bytes read into the buffer, or {@code -1} if there is 
     *         no more data because the end of this {@code InputStream} has been reached.          
     * @throws IOException If the first byte cannot be read for any reason other than end
     *         of {@code RandomAccess}, or if the {@code InputStream} has been closed, or 
     *         if some other I/O error occurs.
     * @throws NullPointerException If {@code b} is {@code null}.
     * @since 1.0
     */
    @Override
    public int read(byte[] b) throws IOException {
        return stream.read(b);
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
     * @since 1.0
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return stream.read(b, off, len);
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
     * @since 1.0
     */
    @Override
    public long skip(long n) throws IOException {
        return stream.skip(n);
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
     * @since 1.0
     */
    @Override
    public int available() throws IOException {
        return stream.available();
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @throws IOException  if an I/O error occurs.
     * @since 1.0
     */
    @Override
    public void close() throws IOException {
        stream.close();
    }

    /**
     * Marks the current position in this input stream. A subsequent call to
     * the <code>reset</code> method repositions this stream at the last marked
     * position so that subsequent reads re-read the same bytes.
     *
     * <p> The <code>readlimit</code> arguments tells this input stream to
     * allow that many bytes to be read before the mark position gets
     * invalidated.
     *
     * <p> Marking a closed stream should not have any effect on the stream.
     * @param readlimit the maximum limit of bytes that can be read before
     *        the mark position becomes invalid.
     * @see java.io.InputStream#reset()
     * @since 1.0
     */
    @Override
    public void mark(int readlimit) {
        stream.mark(readlimit);
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * @throws IOException if this stream has not been marked or if the
     *         mark has been invalidated.
     * @see java.io.InputStream#mark(int)
     * @see java.io.IOException
     * @since 1.0
     */
    @Override
    public void reset() throws IOException {
        stream.reset();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public boolean markSupported() {
        return stream.markSupported();
    }
    
}
