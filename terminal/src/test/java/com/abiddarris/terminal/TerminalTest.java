package com.abiddarris.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.common.utils.ObjectWrapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.concurrent.ExecutionException;

class TerminalTest {

    private Terminal terminal;
    private Command mockCommand;

    @BeforeEach
    void setUp() {
        terminal = new Terminal();
        mockCommand = new MockCommand();
    }

    @Test
    void testAddAndGetCommand() {
        // Add a command
        terminal.addCommand("mock", mockCommand);

        // Retrieve the added command
        Command command = terminal.getCommand("mock");

        // Assert that the command is correctly retrieved
        assertNotNull(command, "Command should not be null");
        assertEquals(mockCommand, command, "Retrieved command should be the same as the one added");
    }

    @Test
    void testAddCommandOnExistingName() {
        // Add a command
        terminal.addCommand("mock", mockCommand);

        Command newCommand = context -> 0;
        terminal.addCommand("mock", newCommand);

        Command command = terminal.getCommand("mock");

        assertNotNull(command, "Command should not be null");
        assertEquals(newCommand, command, "Retrieved command should be the same as the one added");
    }

    @Test
    void nullTestOnAddCommand() {
        assertThrows(NullPointerException.class, () -> terminal.addCommand(null, mockCommand));
        assertThrows(NullPointerException.class, () -> terminal.addCommand("mock", null));
    }

    @Test
    void testGetFromTerminalChild() {
        // Add a command
        terminal.addCommand("mock", mockCommand);

        Terminal subterminal = terminal.newTerminal();
        assertEquals(subterminal.getCommand("mock"), mockCommand);
    }

    @Test
    void testExecuteValidCommand() throws ExecutionException, InterruptedException {
        // Add a mock command
        terminal.addCommand("mock", mockCommand);

        // Execute a valid command
        String commandLine = "mock";
        Process process = terminal.execute(commandLine);

        // Assert that process is not null and that the command is executed
        assertEquals(process.getResultCode(), 0);
    }

    @Test
    void testExecuteInvalidCommand() {
        // Try executing an invalid command (no command added with this name)
        String commandLine = "invalidCommand";

        // Expect CommandNotFoundException
        CommandNotFoundException exception = assertThrows(CommandNotFoundException.class, () -> {
            terminal.execute(commandLine);
        });
        assertEquals("Command 'invalidCommand' not found", exception.getMessage());
    }

    @Test
    void testExecuteEmptyCommand() {
        assertThrows(CommandException.class, () -> terminal.execute(""));
    }

    @Test
    void executeFromChild() throws ExecutionException, InterruptedException {
        ObjectWrapper<Boolean> called = new ObjectWrapper<>(false);
        terminal.addCommand("mock", context -> {
            called.setObject(true);
            return 0;
        });

        Terminal subterminal = terminal.newTerminal();
        Process process = subterminal.execute("mock");

        assertEquals(0, process.getResultCode());
        assertTrue(called.getObject());
    }

    @Test
    void getParentTerminal() {
        assertEquals(terminal.newTerminal().getParentTerminal(), terminal);
    }

    @Test
    void setNullParser() {
        assertThrows(NullPointerException.class, () -> terminal.setParser(null));
    }

    @Test
    void parserThatReturnsNull() {
        terminal.setParser(str -> null);
        assertThrows(NullPointerException.class, () -> terminal.execute("hai"));
    }

    @Test
    void testSetAndGetEnv() {
        // Set an environment variable
        terminal.setEnv("TEST_VAR", "test_value");

        // Retrieve the environment variable
        String value = terminal.getEnv("TEST_VAR");

        // Assert that the environment variable is set and retrieved correctly
        assertEquals("test_value", value, "Environment variable value should match the set value");
    }

    @Test
    void testClearEnv() {
        // Set and then clear the environment variable
        terminal.setEnv("CLEAR_VAR", "clear_value");
        assertTrue(terminal.clearEnv("CLEAR_VAR"), "clearEnv should return true when removing an existing variable");

        // Ensure that the environment variable is cleared
        String value = terminal.getEnv("CLEAR_VAR");
        assertNull(value, "Environment variable should be null after clearing");
    }

    @Test
    void testGetWorkingDirectory() {
        // Test the working directory of the terminal
        File workingDirectory = terminal.getWorkingDirectory();

        // Assert that the working directory is the current directory or as expected
        assertNotNull(workingDirectory, "Working directory should not be null");
        assertEquals(new File(""), workingDirectory);
    }

    @Test
    void testSetWorkingDirectory() {
        // Set a new working directory
        File newDirectory = new File("/tmp");
        terminal.setWorkingDirectory(newDirectory);

        // Assert that the working directory is set correctly
        assertEquals(newDirectory, terminal.getWorkingDirectory(), "Working directory should be set to /tmp");
    }

    @Test
    void getWorkingDirectoryFromChild() {
        File workingDirectory = terminal.getWorkingDirectory().getAbsoluteFile().getParentFile();
        terminal.setWorkingDirectory(workingDirectory);

        assertEquals(workingDirectory, terminal.newTerminal().getWorkingDirectory());
    }

    @Test
    void setWorkingDirectoryInChild() {
        File workingDirectory = terminal.getWorkingDirectory();
        File childWorkingDirectory = new File("/usr");

        Terminal child = terminal.newTerminal();
        child.setWorkingDirectory(childWorkingDirectory);

        assertEquals(workingDirectory, terminal.getWorkingDirectory());
        assertEquals(childWorkingDirectory, child.getWorkingDirectory());
    }

    @Test
    void passNullOnsetWorkingDirectory() {
        assertThrows(NullPointerException.class, () -> terminal.setWorkingDirectory(null));
    }

    @Test
    void setEnvAndHasEnvFromChild() {
        terminal.setEnv("user", "Cat");

        Terminal subTerminal = terminal.newTerminal();
        subTerminal.setEnv("user", "Dog");

        assertTrue(subTerminal.hasEnv("user"));
        assertEquals("Dog", subTerminal.getEnv("user"));
    }

    @Test
    void getEnvAndHasEnvFromChild() {
        terminal.setEnv("user", "Cat");

        Terminal subTerminal = terminal.newTerminal();

        assertTrue(subTerminal.hasEnv("user"));
        assertEquals("Cat", subTerminal.getEnv("user"));
    }

    @Test
    void clearFromChild() {
        terminal.setEnv("user", "Cat");

        Terminal subTerminal = terminal.newTerminal();

        assertFalse(subTerminal.clearEnv("user"));
        assertEquals("Cat", terminal.getEnv("user"));
    }

    @Test
    void clearEnvByCallingSetEnv() {
        terminal.setEnv("user", "Cat");
        terminal.setEnv("user", null);

        assertNull(terminal.getEnv("user"));
    }


    // Mock Command class to simulate command execution (for unit testing)
    static class MockCommand implements Command {
        @Override
        public int main(Context context) {
            // Simulate command execution logic
            return 0;
        }
    }
}
