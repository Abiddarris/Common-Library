package com.abiddarris.terminal;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Terminal {

    private final Map<String, Command> commands = new HashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private Parser parser = new DefaultParser();

    public void addCommand(String name, Command command) {
        checkNonNull(name, "name cannot be null");
        checkNonNull(command, "command cannot be null");

        commands.put(name, command);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public Process execute(String command) {
        if (command.isBlank()) {
            throw new CommandException("command is blank");
        }

        String[] args = parser.parse(command);
        checkNonNull(args, "Parser.parse() cannot return null");

        Command commandObj = getCommand(args[0]);
        if (commandObj == null) {
            throw new CommandNotFoundException(String.format("Command '%s' not found", args[0]));
        }

        PipedInputStream inInPipe, outInPipe;
        PipedOutputStream inOutPipe, outOutPipe;
        try {
            inInPipe = new PipedInputStream();
            inOutPipe = new PipedOutputStream(inInPipe);
            outInPipe = new PipedInputStream();
            outOutPipe = new PipedOutputStream(outInPipe);
        } catch (IOException e) {
            throw new CommandException("Unable to create pipe");
        }

        Context context = new Context(this, args, outOutPipe, inInPipe);
        Future<Integer> future = executor.submit(() -> {
            try {
                int result = commandObj.main(context);
                inOutPipe.close();
                outOutPipe.close();

                return result;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        return new Process(future, outInPipe, inOutPipe);
    }

    public void setParser(Parser parser) {
        checkNonNull(parser, "parser cannot be null");

        this.parser = parser;
    }
}
