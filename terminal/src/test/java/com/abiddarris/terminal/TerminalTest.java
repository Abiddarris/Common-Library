package com.abiddarris.terminal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Map;
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

    // Mock Command class to simulate command execution (for unit testing)
    static class MockCommand implements Command {
        @Override
        public int main(Context context) {
            // Simulate command execution logic
            return 0;
        }
    }
}
