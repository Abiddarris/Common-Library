package com.abiddarris.terminal;

public class CommandNotFoundException extends CommandException {

    public CommandNotFoundException() {
    }

    public CommandNotFoundException(Throwable cause) {
        super(cause);
    }

    public CommandNotFoundException(String message) {
        super(message);
    }

    public CommandNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
