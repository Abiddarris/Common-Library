package com.abiddarris.terminal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abiddarris.terminal.arguments.ArgumentParser;
import com.abiddarris.terminal.arguments.ArgumentParserException;
import com.abiddarris.terminal.arguments.PositionalArgument;
import com.abiddarris.terminal.arguments.parsers.StringParser;
import com.abiddarris.terminal.arguments.validators.PermittedValueValidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CommandTest {

    private Terminal terminal;
    private CommandImpl command;

    @BeforeEach
    void setUp() {
        terminal = new Terminal();
        command = new CommandImpl();
    }

    @Test
    void nonInteractiveModeTest() throws IOException {
        terminal.setInteractive(false);
        terminal.addCommand("interact", command);

        Process process = terminal.execute("interact");
        Context context = command.getContext();

        assertThrows(IOException.class, () -> process.getOutputStream().write(0));
        assertThrows(IOException.class, () -> context.getInputStream().read());

        command.release();
    }

    @Test
    void interactiveModeTest() throws IOException {
        terminal.addCommand("interact", command);

        Process process = terminal.execute("interact");
        Context context = command.getContext();

        process.getOutputStream().write('m');
        assertEquals('m', context.getInputStream().read());

        command.release();
    }

    @Test
    void commandInputTest() throws IOException {
        terminal.addCommand("interact", command);

        Process process = terminal.execute("interact");
        Context context = command.getContext();

        context.getOutputStream().write('m');
        assertEquals('m', process.getInputStream().read());

        command.release();
    }

    @Test
    void commandErrorTest() throws IOException {
        terminal.addCommand("interact", command);

        Process process = terminal.execute("interact");
        Context context = command.getContext();

        context.getErrorStream().write('m');
        assertEquals('m', process.getErrorStream().read());

        command.release();
    }

    @Test
    void getArgsTest() throws IOException {
        terminal.addCommand("print", command);
        terminal.execute("print hi");

        Context context = command.getContext();
        assertArrayEquals(new String[] {"hi"}, context.getArgs());

        command.release();
    }

    @Test
    void getCommandNameTest() throws IOException {
        terminal.addCommand("print", command);
        terminal.execute("print");

        Context context = command.getContext();
        assertEquals("print", context.getCommandName());

        command.release();
    }

    @Test
    void positionalArgumentTest() {
        terminal.addCommand("print", command);
        terminal.execute("print hi");

        Context context = command.getContext();
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        parser.parse(context.getArgs());

        assertEquals("hi", message.getValue());

        command.release();
    }

    @Test
    void positionalArgumentTest_unexpectedArgument() {
        terminal.addCommand("print", command);
        terminal.execute("print hi hi2");

        Context context = command.getContext();
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        try {
            assertThrows(ArgumentParserException.class, () -> parser.parse(context.getArgs()));
        } finally {
            command.release();
        }
    }

    @Test
    void positionalArgumentTest_missingArgument() {
        terminal.addCommand("print", command);
        terminal.execute("print");

        Context context = command.getContext();
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        try {
            assertThrows(ArgumentParserException.class, () -> parser.parse(context.getArgs()));
        } finally {
            command.release();
        }
    }

    @Test
    void positionalArgumentTest_optionalArgumentTestAndDefaultValueTest_withOptionalArgumentNotProvided() {
        terminal.addCommand("print", command);
        terminal.execute("print hello");

        Context context = command.getContext();
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        PositionalArgument<String> message2 = new PositionalArgument<>("message2", StringParser.INSTANCE);
        message2.setValue("Earth");

        try {
            ArgumentParser parser = new ArgumentParser();
            parser.require(message);
            parser.optional(message2);
            parser.parse(context.getArgs());

            assertEquals("hello", message.getValue());
            assertEquals("Earth", message2.getValue());
        } finally {
            command.release();
        }
    }

    @Test
    void positionalArgumentTest_optionalArgumentTestAndDefaultValueTest_providedOptionalArgument() {
        terminal.addCommand("print", command);
        terminal.execute("print hello world");

        Context context = command.getContext();
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        PositionalArgument<String> message2 = new PositionalArgument<>("message2", StringParser.INSTANCE);
        message2.setValue("Earth");

        try {
            ArgumentParser parser = new ArgumentParser();
            parser.require(message);
            parser.optional(message2);
            parser.parse(context.getArgs());

            assertEquals("hello", message.getValue());
            assertEquals("world", message2.getValue());
        } finally {
            command.release();
        }
    }

    @Test
    void defaultValueResetToNullIfAddedToRequired() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        message.setValue("Hello");

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        assertNull(message.getValue());
    }

    @Test
    void addRequiredPositionalArgumentAfterOptionalOne() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        PositionalArgument<String> message2 = new PositionalArgument<>("message2", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.optional(message);
        assertThrows(IllegalStateException.class, () -> parser.require(message2));
    }

    @Test
    void addDuplicateOptionalPositionalArgument() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.optional(message);
        assertThrows(IllegalArgumentException.class, () -> parser.optional(message));
    }

    @Test
    void addDuplicateRequiredPositionalArgument() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        assertThrows(IllegalArgumentException.class, () -> parser.require(message));
    }

    @Test
    void requiredPositionalArgument_withPossibleValues() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "level", StringParser.INSTANCE,
                new PermittedValueValidator<>("debug", "warning")
        );
        String[] args = {"debug"};

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        parser.parse(args);
    }

    @Test
    void requiredPositionalArgument_withPossibleValues_notMatch() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "level", StringParser.INSTANCE,
                new PermittedValueValidator<>("debug", "warning")
        );
        String[] args = {"verbose"};

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        assertThrows(ArgumentParserException.class, () -> parser.parse(args));
    }

    @Test
    void requiredPositionalArgument_withPossibleValues_notProvided() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "level", StringParser.INSTANCE,
                new PermittedValueValidator<>("debug", "warning")
        );
        String[] args = {};

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        assertThrows(ArgumentParserException.class, () -> parser.parse(args));
    }

    @Test
    void optionalPositionalArgument_withPossibleValues_notProvided() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "level", StringParser.INSTANCE,
                new PermittedValueValidator<>("debug", "warning")
        );
        String[] args = {};

        ArgumentParser parser = new ArgumentParser();
        parser.optional(message);
        parser.parse(args);
    }

    private static class CommandImpl implements Command {

        private final Object contextLock = new Object();
        private final Object releaseLock = new Object();

        private volatile Context context;

        @Override
        public int main(Context context) throws Throwable {
            this.context = context;
            synchronized (contextLock) {
                contextLock.notify();
            }

            synchronized (releaseLock) {
                releaseLock.wait();
            }
            return 0;
        }

        public void release() {
            synchronized (releaseLock) {
                releaseLock.notify();
            }
        }

        public Context getContext() {
            synchronized (contextLock) {
                while (context == null) {
                    try {
                        contextLock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            return context;
        }
    }
}
