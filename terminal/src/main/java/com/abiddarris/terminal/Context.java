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
import java.io.PipedOutputStream;

public class Context {

    private final Terminal terminal;
    private final String[] args;
    private final OutputStream outputStream;
    private final InputStream inputStream;
    private final PipedOutputStream errorStream;

    Context(Terminal terminal, String[] args, OutputStream outputStream, InputStream inputStream, PipedOutputStream errorStream) {
        this.terminal = terminal;
        this.args = args;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.errorStream = errorStream;
    }

    public String[] getArgs() {
        return args;
    }

    public Terminal getTerminal() {
        return terminal;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public PipedOutputStream getErrorStream() {
        return errorStream;
    }
}
