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

        Context context = new Context(newTerminal(), args, outOutPipe, inInPipe, errOutPipe);
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

    public Terminal newTerminal() {
        return new SubTerminal(this);
    }

    public void setParser(Parser parser) {
        checkNonNull(parser, "parser cannot be null");

        this.parser = parser;
    }
}
