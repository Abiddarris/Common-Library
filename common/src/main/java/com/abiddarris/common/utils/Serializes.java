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
package com.abiddarris.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class Serializes {

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serialize(object, output);

        return output.toByteArray();
    }

    public static void serialize(Object object, OutputStream output) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(output)) {
            oos.writeObject(object);
            oos.flush();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        return deserialize(new ByteArrayInputStream(bytes));
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(InputStream input) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(input)) {
            return (T) stream.readObject();
        }
    }
}
