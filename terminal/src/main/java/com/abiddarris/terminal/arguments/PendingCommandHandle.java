package com.abiddarris.terminal.arguments;

import com.abiddarris.terminal.Command;
import com.abiddarris.terminal.Context;
import com.abiddarris.terminal.Terminal;

import java.io.InputStream;
import java.io.OutputStream;

public class PendingCommandHandle {

    private final String[] args;
    private final Command command;

    PendingCommandHandle(Command command, String[] args) {
        this.command = command;
        this.args = args;
    }

    public int execute(Context context) throws Throwable {
        ContextWrapper contextWrapper = new ContextWrapper(
                context.getTerminal(),
                args,
                context.getOutputStream(),
                context.getInputStream(),
                context.getErrorStream()
        );
        return command.main(contextWrapper);
    }

    private static class ContextWrapper extends Context {

        protected ContextWrapper(Terminal terminal, String[] args, OutputStream outputStream, InputStream inputStream, OutputStream errorStream) {
            super(terminal, args, outputStream, inputStream, errorStream);
        }
    }
}
