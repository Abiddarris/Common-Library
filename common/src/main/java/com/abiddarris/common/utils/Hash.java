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
package com.abiddarris.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that create hash from specified {@code InputStream}
 */
public class Hash {
    
    /**
     * Create SHA-256 from a path
     *
     * @param stream {@code InputStream} to hash
     * @param out OutputStream to write after calling {@code InputStream.read()}
     * @return Hash in hex
     * @throws IOException If hashing failed
     */
    public static String createHashingFrom(InputStream stream, OutputStream out) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }

        try (DigestInputStream inputStream = new DigestInputStream(stream, digest)){
            byte[] buf = new byte[8192];
            int len;
            while((len = inputStream.read(buf)) != - 1) {
                out.write(buf,0,len);
            }

            byte[] hashBytes = digest.digest();
            return toHexString(hashBytes);
        }
    }
    
    /**
     * Convert array of Base 10 to Base 16 String
     *
     * @param data {@code Array} of Base 10
     * @return Base 16 {@code String}
     */
    private static String toHexString(byte[] data) {
        StringBuilder builder = new StringBuilder();
        for(byte b : data) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
