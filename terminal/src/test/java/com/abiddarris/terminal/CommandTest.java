package com.abiddarris.terminal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.abiddarris.common.utils.ObjectWrapper;
import com.abiddarris.terminal.arguments.ArgumentParser;
import com.abiddarris.terminal.arguments.PendingCommandHandle;
import com.abiddarris.terminal.arguments.PositionalArgument;
import com.abiddarris.terminal.arguments.parsers.StringParser;

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
    void subcommandTest() throws Throwable {
        terminal.addCommand("util", command);
        terminal.execute("util print hi");

        Context context = command.getContext();
        ArgumentParser parser = new ArgumentParser();
        ObjectWrapper<Context> subcommandContext = new ObjectWrapper<>();
        parser.registerCommand("print", (ctx) -> {
            subcommandContext.setObject(ctx);
            return 0;
        });

        PendingCommandHandle handle = parser.parse(context.getArgs());
        assertEquals(0, handle.execute(context));

        Context subcommandCtx = subcommandContext.getObject();
        assertArrayEquals(new String[] {"hi"}, subcommandCtx.getArgs());
        assertEquals("print", subcommandCtx.getCommandName());
        assertEquals(context.getInputStream(), subcommandCtx.getInputStream());
        assertEquals(context.getOutputStream(), subcommandCtx.getOutputStream());
        assertEquals(context.getErrorStream(), subcommandCtx.getErrorStream());

        command.release();
    }

    @Test
    void addSubcommand_butDoNotExecuteItTest() {
        terminal.addCommand("util", command);
        terminal.execute("util");

        Context context = command.getContext();
        ArgumentParser parser = new ArgumentParser();
        ObjectWrapper<Context> subcommandContext = new ObjectWrapper<>();
        parser.registerCommand("print", (ctx) -> {
            subcommandContext.setObject(ctx);
            return 0;
        });

        PendingCommandHandle handle = parser.parse(context.getArgs());
        assertNull(handle);

        command.release();
    }

    @Test
    void subcommandTest_butDoNotExecuteItAndCheckParentCommandArgs() {
        terminal.addCommand("util", command);
        terminal.execute("util hi");

        PositionalArgument<String> arg = new PositionalArgument<>("arg", StringParser.INSTANCE);

        Context context = command.getContext();
        ArgumentParser parser = new ArgumentParser();
        parser.require(arg);

        ObjectWrapper<Context> subcommandContext = new ObjectWrapper<>();
        parser.registerCommand("print", (ctx) -> {
            subcommandContext.setObject(ctx);
            return 0;
        });

        PendingCommandHandle handle = parser.parse(context.getArgs());
        assertNull(handle);
        assertEquals("hi", arg.getValue());

        command.release();
    }

    @Test
    void escapedStringTest() {
        terminal.addCommand("print", command);
        execute("print Hello\\ World", context -> {
            assertArrayEquals(new String[] {"Hello World"}, context.getArgs());
        });
    }

    @Test
    void doubleSpacesTest() {
        terminal.addCommand("print", command);
        execute("print Hello  World", context -> {
            assertArrayEquals(new String[] {"Hello", "World"}, context.getArgs());
        });
    }

    private void execute(String command, ContextConsumer contextConsumer) {
        terminal.execute(command);

        try {
            contextConsumer.onContext(this.command.getContext());
        } finally {
            this.command.release();
        }
    }

    private interface ContextConsumer {
        void onContext(Context context);
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
