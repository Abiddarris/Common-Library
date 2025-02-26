package com.abiddarris.terminal;

import static com.abiddarris.common.utils.Preconditions.checkNonNull;

import com.abiddarris.common.utils.Exceptions;

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

        PipedInputStream inInPipe, outInPipe, errInPipe;
        PipedOutputStream inOutPipe, outOutPipe, errOutPipe;
        try {
            inInPipe = new PipedInputStream();
            inOutPipe = new PipedOutputStream(inInPipe);
            outInPipe = new PipedInputStream();
            outOutPipe = new PipedOutputStream(outInPipe);
            errInPipe = new PipedInputStream();
            errOutPipe = new PipedOutputStream(errInPipe);
        } catch (IOException e) {
            throw new CommandException("Unable to create pipe");
        }

        Context context = new Context(this, args, outOutPipe, inInPipe, errOutPipe);
        Future<Integer> future = executor.submit(() -> {
            try {
                return commandObj.main(context);
            } catch (Throwable e) {
                errOutPipe.write(Exceptions.toString(e).getBytes());
                return -1;
            } finally {
                inOutPipe.close();
                outOutPipe.close();
                errOutPipe.close();
            }
        });

        return new Process(future, outInPipe, inOutPipe, errInPipe);
    }

    public void setParser(Parser parser) {
        checkNonNull(parser, "parser cannot be null");

        this.parser = parser;
    }
}
