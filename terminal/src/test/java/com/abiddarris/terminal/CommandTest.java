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
    void spaceInCommandName() {
        terminal.addCommand("print func", command);
        execute("print\\ func Hello  World", context -> {
            assertArrayEquals(new String[] {"Hello", "World"}, context.getArgs());
        });
    }

    @Test
    void doubleSpacesTest() {
        terminal.addCommand("print", command);
        execute("print Hello  World", context -> {
            assertArrayEquals(new String[] {"Hello", "World"}, context.getArgs());
        });
    }

    @Test
    void spaceOnTheEndOfCommand() {
        terminal.addCommand("print", command);
        execute("print Hello World  ", context -> {
            assertArrayEquals(new String[] {"Hello", "World"}, context.getArgs());
        });
    }

    @Test
    void escapedSpaceOnTheEndOfCommand() {
        terminal.addCommand("print", command);
        execute("print Hello World\\ ", context -> {
            assertArrayEquals(new String[] {"Hello", "World "}, context.getArgs());
        });
    }

    @Test
    void spaceOnStartOfCommand() {
        terminal.addCommand("print", command);
        execute(" print Hello World\\ ", context -> {
            assertArrayEquals(new String[] {"Hello", "World "}, context.getArgs());
        });
    }

    @Test
    void doubleQuotedArgument() {
        terminal.addCommand("print", command);
        execute("print \"Hello World\"", context -> {
            assertArrayEquals(new String[] {"Hello World"}, context.getArgs());
        });
    }

    @Test
    void duplicateSpaceInDoubleQuotedArgument() {
        terminal.addCommand("print", command);
        execute("print \"Hello  World\"", context -> {
            assertArrayEquals(new String[] {"Hello  World"}, context.getArgs());
        });
    }

    @Test
    void escapedDoubleQuoteOutsideQuote() {
        terminal.addCommand("print", command);
        execute("print \\\"Hello World\\\"", context -> {
            assertArrayEquals(new String[] {"\"Hello", "World\""}, context.getArgs());
        });
    }

    @Test
    void variableExpansion() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print $USER", context -> {
            assertArrayEquals(new String[] {"Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOther() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print $USER$USER", context -> {
            assertArrayEquals(new String[] {"AbidAbid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpace() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print $USER", context -> {
            assertArrayEquals(new String[] {"Abid", "Darris"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableName() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print $USER/Desktop", context -> {
            assertArrayEquals(new String[] {"Abid/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeText() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print /home/$USER", context -> {
            assertArrayEquals(new String[] {"/home/Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableName() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print $USERDesktop", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSet() {
        terminal.addCommand("print", command);

        execute("print $USER", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyName() {
        terminal.addCommand("print", command);

        execute("print $", context -> {
            assertArrayEquals(new String[] {"$"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidName() {
        terminal.addCommand("print", command);

        execute("print $6d", context -> {
            assertArrayEquals(new String[] {"d"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print ${USER}", context -> {
            assertArrayEquals(new String[] {"Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOtherWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print ${USER}${USER}", context -> {
            assertArrayEquals(new String[] {"AbidAbid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpaceWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print ${USER}", context -> {
            assertArrayEquals(new String[] {"Abid", "Darris"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableNameWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print ${USER}/Desktop", context -> {
            assertArrayEquals(new String[] {"Abid/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeTextWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print /home/${USER}", context -> {
            assertArrayEquals(new String[] {"/home/Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableNameWithCurlyBrackets() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print ${USER}Desktop", context -> {
            assertArrayEquals(new String[] {"AbidDesktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSetWithCurlyBrackets() {
        terminal.addCommand("print", command);

        execute("print ${USER}", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithCurlyBrackets() {
        terminal.addCommand("print", command);

        execute("print ${}", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithEscapedCurlyBrackets() {
        terminal.addCommand("print", command);

        execute("print $\\{\\}", context -> {
            assertArrayEquals(new String[] {"${}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidNameCurlyBrackets() {
        terminal.addCommand("print", command);

        ParseException parseException = assertThrows(ParseException.class, () ->
                execute("print ${6d}", context -> {
                }));
        assertEquals("Bad substitution", parseException.getMessage());
    }

    @Test
    void variableExpansionNumbersCurlyBrackets() {
        terminal.addCommand("print", command);

        execute("print ${72}", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '$USER'", context -> {
            assertArrayEquals(new String[] {"$USER"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOtherQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '$USER$USER'", context -> {
            assertArrayEquals(new String[] {"$USER$USER"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpaceQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print '$USER'", context -> {
            assertArrayEquals(new String[] {"$USER"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableNameQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '$USER/Desktop'", context -> {
            assertArrayEquals(new String[] {"$USER/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeTextQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '/home/$USER'", context -> {
            assertArrayEquals(new String[] {"/home/$USER"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableNameQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '$USERDesktop'", context -> {
            assertArrayEquals(new String[] {"$USERDesktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSetQuoted() {
        terminal.addCommand("print", command);

        execute("print '$USER'", context -> {
            assertArrayEquals(new String[] {"$USER"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameQuoted() {
        terminal.addCommand("print", command);

        execute("print '$'", context -> {
            assertArrayEquals(new String[] {"$"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidNameQuoted() {
        terminal.addCommand("print", command);

        execute("print '$6d'", context -> {
            assertArrayEquals(new String[] {"$6d"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '${USER}'", context -> {
            assertArrayEquals(new String[] {"${USER}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOtherCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '${USER}${USER}'", context -> {
            assertArrayEquals(new String[] {"${USER}${USER}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpaceWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print '${USER}'", context -> {
            assertArrayEquals(new String[] {"${USER}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableNameWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '${USER}/Desktop'", context -> {
            assertArrayEquals(new String[] {"${USER}/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeTextWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '/home/${USER}'", context -> {
            assertArrayEquals(new String[] {"/home/${USER}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableNameWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print '${USER}Desktop'", context -> {
            assertArrayEquals(new String[] {"${USER}Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSetWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);

        execute("print '${USER}'", context -> {
            assertArrayEquals(new String[] {"${USER}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithCurlyBracketsQuoted() {
        terminal.addCommand("print", command);

        execute("print '${}'", context -> {
            assertArrayEquals(new String[] {"${}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithEscapedCurlyBracketsQuoted() {
        terminal.addCommand("print", command);

        execute("print '$\\{\\}'", context -> {
            assertArrayEquals(new String[] {"${}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidNameCurlyBracketsQuoted() {
        terminal.addCommand("print", command);

        execute("print '${6d}'", context -> {
                    assertArrayEquals(new String[] {"${6d}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNumbersCurlyBracketsQuoted() {
        terminal.addCommand("print", command);

        execute("print '${72}'", context -> {
            assertArrayEquals(new String[] {"${72}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"$USER\"", context -> {
            assertArrayEquals(new String[] {"Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOtherDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"$USER$USER\"", context -> {
            assertArrayEquals(new String[] {"AbidAbid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpaceDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print \"$USER\"", context -> {
            assertArrayEquals(new String[] {"Abid Darris"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableNameDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"$USER/Desktop\"", context -> {
            assertArrayEquals(new String[] {"Abid/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeTextDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"/home/$USER\"", context -> {
            assertArrayEquals(new String[] {"/home/Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableNameDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"$USERDesktop\"", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSetDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"$USER\"", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"$\"", context -> {
            assertArrayEquals(new String[] {"$"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidNameDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"$6d\"", context -> {
            assertArrayEquals(new String[] {"d"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"${USER}\"", context -> {
            assertArrayEquals(new String[] {"Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionNextToEachOtherCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"${USER}${USER}\"", context -> {
            assertArrayEquals(new String[] {"AbidAbid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueHasSpaceWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid Darris");

        execute("print \"${USER}\"", context -> {
            assertArrayEquals(new String[] {"Abid Darris"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByInvalidVariableNameWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"${USER}/Desktop\"", context -> {
            assertArrayEquals(new String[] {"Abid/Desktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionAfterSomeTextWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"/home/${USER}\"", context -> {
            assertArrayEquals(new String[] {"/home/Abid"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionFollowedByValidVariableNameWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);
        terminal.setVariable("USER", "Abid");

        execute("print \"${USER}Desktop\"", context -> {
            assertArrayEquals(new String[] {"AbidDesktop"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionValueDoesNotSetWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"${USER}\"", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"${}\"", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void variableExpansionEmptyNameWithEscapedCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"$\\{\\}\"", context -> {
            assertArrayEquals(new String[] {"${}"}, context.getArgs());
        });
    }

    @Test
    void variableExpansionInvalidNameCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);

        ParseException parseException = assertThrows(ParseException.class, () ->
                execute("print \"${6d}\"", context -> {
                }));
        assertEquals("Bad substitution", parseException.getMessage());
    }

    @Test
    void variableExpansionNumbersCurlyBracketsDoubleQuoted() {
        terminal.addCommand("print", command);

        execute("print \"${72}\"", context -> {
            assertArrayEquals(new String[] {""}, context.getArgs());
        });
    }

    @Test
    void simpleAssignment() {
        terminal.execute("data=HelloWorld");

        assertEquals("HelloWorld", terminal.getVariable("data"));
    }

    @Test
    void simpleAssignment_startWithNumber() {
        Exception e = assertThrows(CommandNotFoundException.class, () ->
                terminal.execute("4data=HelloWorld"));
        assertEquals("Command '4data=HelloWorld' not found", e.getMessage());
    }

    @Test
    void simpleAssignment_invalidKeyName() {
        Exception e = assertThrows(CommandNotFoundException.class, () ->
                terminal.execute("da#ta=HelloWorld"));
        assertEquals("Command 'da#ta=HelloWorld' not found", e.getMessage());
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
