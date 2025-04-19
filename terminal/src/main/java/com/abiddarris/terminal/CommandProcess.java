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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

class CommandProcess implements Process {

    private final Future<Integer> future;
    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final InputStream errorStream;

    CommandProcess(Future<Integer> future, InputStream inputStream, OutputStream outputStream, InputStream errorStream) {
        this.future = future;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.errorStream = errorStream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public int getResultCode() throws ExecutionException, InterruptedException {
        return future.get();
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getErrorStream() {
        return errorStream;
    }
}
