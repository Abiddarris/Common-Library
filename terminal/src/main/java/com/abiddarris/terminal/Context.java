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
