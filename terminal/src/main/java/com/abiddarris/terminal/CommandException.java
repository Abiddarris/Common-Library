package com.abiddarris.terminal;

public class CommandException extends RuntimeException {

    public CommandException() {
    }

    public CommandException(Throwable cause) {
        super(cause);
    }

    public CommandException(String message) {
        super(message);
    }

    public CommandException(String message, Throwable cause) {
        super(message, cause);
    }
}
