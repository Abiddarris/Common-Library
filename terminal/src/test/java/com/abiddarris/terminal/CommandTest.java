package com.abiddarris.terminal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
