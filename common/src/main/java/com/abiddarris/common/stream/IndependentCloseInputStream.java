package com.abiddarris.common.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@code InputStream} implementation that does not close associated {@code InputStream}
 * when {@link #close()} called
 *
 * <p>When {@link #close()} has been called, only this stream is closed. For example any call
 * to {@link #read()} is throwing {@code IOException} if {@link #close()} has been called.
 * but this class does not close {@code InputStream} that passed to constructor. Meaning any call
 * to {@code InputStream} does not throw an {@code Exception} even though this stream already closed.
 * unless there is a call to {@code close()} to {@code InputStream}
 *
 * @author Abiddarris
 * @since 1.0
 */
public class IndependentCloseInputStream extends DelegateInputStream {
    
    /**
     * Hold this class state
     */
    private CloseableObject closeObject = new CloseableObject();
    
    /**
     * Create new {@code IndependentCloseInputStream} from specified stream
     *
     * @param stream An existing stream
     * @throws NullPointerException If {@code stream} is {@code null}
     * @since 1.0
     */
    public IndependentCloseInputStream(InputStream stream) {
        super(stream);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int read() throws IOException {
        closeObject.ensureOpen();
        
        return super.read();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int read(byte[] b) throws IOException {
        closeObject.ensureOpen();      
        
        return super.read(b);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        closeObject.ensureOpen();        
        
        return super.read(b, off, len);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public long skip(long n) throws IOException {
        closeObject.ensureOpen();      
        
        return super.skip(n);
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public int available() throws IOException {
        closeObject.ensureOpen();        
        
        return super.available();
    }

    /**
     * Close this {@code IndependentCloseInputStream} without closing passed
     * {@code InputStream}.
     *
     * @since 1.0
     */
    @Override
    public void close() {
        closeObject.close();
    }

    /**
     * {@inheritDoc}
     *
     * @since 1.0
     */
    @Override
    public void reset() throws IOException {
        closeObject.ensureOpen();        
        
        super.reset();
    }
    
}
