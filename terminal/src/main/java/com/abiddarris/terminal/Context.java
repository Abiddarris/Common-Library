package com.abiddarris.terminal;

import java.io.OutputStream;
import java.io.PipedOutputStream;

public class Context {

    private final Terminal terminal;
    private final String[] args;
    private final OutputStream outputStream;

    Context(Terminal terminal, String[] args, OutputStream outputStream) {
        this.terminal = terminal;
        this.args = args;
        this.outputStream = outputStream;
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
}
