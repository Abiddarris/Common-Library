package com.abiddarris.terminal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;

public class Context {

    private final Terminal terminal;
    private final String[] args;
    private final OutputStream outputStream;
    private final InputStream inputStream;

    Context(Terminal terminal, String[] args, OutputStream outputStream, InputStream inputStream) {
        this.terminal = terminal;
        this.args = args;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
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
}
