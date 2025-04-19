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
package com.abiddarris.terminal;

import com.abiddarris.common.stream.NullOutputStream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;

public class AssignVariableProcess implements Process {

    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();
    private final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public int getResultCode() throws ExecutionException, InterruptedException {
        return 0;
    }

    @Override
    public OutputStream getOutputStream() {
        return NULL_OUTPUT_STREAM;
    }

    @Override
    public InputStream getErrorStream() {
        return inputStream;
    }
}
