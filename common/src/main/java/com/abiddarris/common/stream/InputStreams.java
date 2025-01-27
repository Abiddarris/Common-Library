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

import static java.util.Arrays.copyOf;

import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

/**
 * Class that provides utilities for {@code InputStream}.
 *
 * @since 1.0
 * @author Abiddarris
 */
public final class InputStreams {
    
    /**
     * Prevent from being created
     */
    private InputStreams() {
    }
    
    /**
     * Reads {@code InputStream} till newline or the end of file
     * 
     * @param stream {@code InputStream} to read
     * @throws IOException If an error occurs while reading the stream
     * @return readed data in bytes of array
     */
    public static byte[] readLine(InputStream stream) throws IOException {
    	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int byte0;
        while((byte0 = stream.read()) != -1 && byte0 != '\n') {
            outputStream.write(byte0);
        }
        
        return outputStream.toByteArray();
    }
    
    /**
     * Attempt to read exactly {@code n} bytes. 
     *
     * <p>If the stream already at the end of stream, it returns 
     * empty array. If this method encounter end of stream before
     * read exactly {@code n}, it trims the array to how many bytes are read.
     *
     * @param stream {@code InputStream}
     * @param n How many byte to read
     * @throws IOException if I/O error occurs while reading the stream
     * @return Array of bytes containing the data
     * @since 1.0
     */
    public static byte[] readExact(InputStream stream, int n) throws IOException {
        byte[] b = new byte[n];
        int len = stream.read(b);
        if(len == -1) {
            return new byte[0];
        }
                
        while(len != n) {
            int readed = stream.read(b, len, b.length - len);
            if(readed == -1) {
                break;
            }
            len += readed;
        }
                
        if(len != n) {
            b = copyOf(b, len);
        } 
            
        return b;
    }
    
    /**
     * Reads all bytes from given {@code InputStream}.
     *
     * @param stream {@code InputStream} to read
     * @throws IOException if I/O error occurs while reading the stream
     * @return Array of bytes that contains the data
     * @since 1.0
     */
    public static byte[] readAll(InputStream stream) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buf = new byte[1024 * 8];
        int len;
        while((len = stream.read(buf)) != -1) {
            output.write(buf, 0, len);
        }
            
        return output.toByteArray();
    }

    /**
     * Reads all bytes from given {@code InputStream} as a string.
     *
     * @param reader {@code Reader} to read
     * @return String contains the data
     * @throws IOException if I/O error occurs while reading the stream
     * @since 1.0
     */
    public static String readAll(Reader reader) throws IOException {
        CharArrayWriter output = new CharArrayWriter();
        char[] buf = new char[1024 * 8];
        int len;
        while((len = reader.read(buf)) != -1) {
            output.write(buf, 0, len);
        }

        return output.toString();
    }
    
    /**
     * Attempt to exactly skip {@code n} bytes
     *
     * <p>If the stream already at the end of stream, it returns 
     * {@code 0}. If this method encounter end of stream before
     * skip exactly {@code n} bytes, it returns how many bytes are skipped.
     *
     * @param stream {@code InputStream}
     * @param n How many byte to skip
     * @throws IOException if I/O error occurs while skipping the stream
     * @return How many bytes are skipped
     * @since 1.0
     */
    public static long skipExact(InputStream stream, long n) throws IOException {
        if(n <= 0) {
            return 0;
        }
        
        long skipped = 0;
        long skip = n;
        while(skipped != n) {
            long delta = stream.skip(skip);
            if(delta <= 0) {
                if(stream.read() == -1)
                    break;
                delta = 1;
            }
            skip = skip - delta;
            skipped += delta;
        }
        
        return skipped;
    }
    
    /**
     * Discard all remaining bytes from the input stream.
     *
     * @param stream {@code InputStream}
     * @throws IOException if an I/O error occurs while reading the stream
     * @since 1.0
     */
    public static void discardAll(InputStream stream) throws IOException {
    	do {
            skipExact(stream, Long.MAX_VALUE);
        } while(stream.read() != -1);
    }
    
    /**
     * Write all bytes from given {@code InputStream} to given {@code OutputStream}
     *
     * @param src Bytes source
     * @param dest Write destination
     * @throws IOException if an I/O error occurs while reading the stream
     * @since 1.0
     */
    public static void writeAllTo(InputStream src, OutputStream dest) throws IOException {
        int len;
        byte[] buf = new byte[8192];
        while((len = src.read(buf)) != -1) {
            dest.write(buf, 0, len);
        }
        dest.flush();
    }
}
