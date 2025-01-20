package com.abiddarris.terminal;

public class Context {

    private final Terminal terminal;
    private final String[] args;

    Context(Terminal terminal, String[] args) {
        this.terminal = terminal;
        this.args = args;
    }

    public String[] getArgs() {
        return args;
    }

    public Terminal getTerminal() {
        return terminal;
    }
}
