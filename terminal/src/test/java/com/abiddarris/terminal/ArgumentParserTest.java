package com.abiddarris.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.abiddarris.terminal.arguments.ArgumentParser;
import com.abiddarris.terminal.arguments.ArgumentParserException;
import com.abiddarris.terminal.arguments.Flag;
import com.abiddarris.terminal.arguments.MultipleValueOption;
import com.abiddarris.terminal.arguments.Option;
import com.abiddarris.terminal.arguments.PositionalArgument;
import com.abiddarris.terminal.arguments.UnlimitedPositionalArgument;
import com.abiddarris.terminal.arguments.parsers.StringParser;
import com.abiddarris.terminal.arguments.validators.PermittedValueValidator;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class ArgumentParserTest {

    @Test
    void positionalArgumentTest() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        parser.parse(new String[] {"hi"});

        assertEquals("hi", message.getValue());
    }

    @Test
    void positionalArgumentTest_unexpectedArgument() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        assertThrows(ArgumentParserException.class, () -> parser.parse(new String[]{"hi", "hi2"}));
    }

    @Test
    void positionalArgumentTest_missingArgument() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        assertThrows(ArgumentParserException.class, () -> parser.parse(new String[]{}));
    }

    @Test
    void positionalArgumentTest_optionalArgumentTestAndDefaultValueTest_withOptionalArgumentNotProvided() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        PositionalArgument<String> message2 = new PositionalArgument<>("message2", StringParser.INSTANCE);
        message2.setValue("Earth");


        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        parser.optional(message2);
        parser.parse(new String[]{"hello"});

        assertEquals("hello", message.getValue());
        assertEquals("Earth", message2.getValue());
    }

    @Test
    void positionalArgumentTest_optionalArgumentTestAndDefaultValueTest_providedOptionalArgument() {
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);
        PositionalArgument<String> message2 = new PositionalArgument<>("message2", StringParser.INSTANCE);
        message2.setValue("Earth");

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);
        parser.optional(message2);
        parser.parse(new String[]{"hello", "world"});

        assertEquals("hello", message.getValue());
        assertEquals("world", message2.getValue());
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
        PositionalArgument<String> level = new PositionalArgument<>(
                "level", StringParser.INSTANCE,
                new PermittedValueValidator<>("debug", "warning")
        );
        String[] args = {};

        ArgumentParser parser = new ArgumentParser();
        parser.optional(level);
        parser.parse(args);
    }

    @Test
    void optional_unlimitedTwoPositionalArgument() {
        PositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> messages2 = new UnlimitedPositionalArgument<>(
                "message2", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.optional(messages);
        assertThrows(IllegalArgumentException.class, () -> parser.optional(messages2));
    }

    @Test
    void required_unlimitedTwoPositionalArgument() {
        PositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> messages2 = new UnlimitedPositionalArgument<>(
                "message2", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.require(messages);
        assertThrows(IllegalArgumentException.class, () -> parser.require(messages2));
    }

    @Test
    void requiredOptional_unlimitedTwoPositionalArgument() {
        PositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> messages2 = new UnlimitedPositionalArgument<>(
                "message2", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.require(messages);
        assertThrows(IllegalArgumentException.class, () -> parser.optional(messages2));
    }

    @Test
    void optionalArgumentAfter_unlimitedPositionalArgument() {
        PositionalArgument<String> messages = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> messages2 = new UnlimitedPositionalArgument<>(
                "message2", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.require(messages2);
        assertThrows(IllegalArgumentException.class, () -> parser.optional(messages));
    }

    @Test
    void requiredUnlimitedPositionalArgumentAtTheEnd_satisfied() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John", "Hi", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receiver);
        parser.require(messages);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receiver.getValue());
        assertEquals(args[2], messages.getValue());
        assertEquals(List.of(args[2], args[3]), messages.getValues());
    }

    @Test
    void requiredUnlimitedPositionalArgumentAtTheEnd_notSatisified() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receiver);
        parser.require(messages);

        Exception exception = assertThrows(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing 3rd argument (messages)", exception.getMessage());
    }

    @Test
    void optionalUnlimitedPositionalArgumentAtTheEnd_satisfied() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John", "Hi", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receiver);
        parser.optional(messages);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receiver.getValue());
        assertEquals(args[2], messages.getValue());
        assertEquals(List.of(args[2], args[3]), messages.getValues());
    }

    @Test
    void optionalUnlimitedPositionalArgumentAtTheEnd_notSatisified() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receiver);
        parser.optional(messages);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receiver.getValue());
        assertNull(messages.getValue());
        assertEquals(Collections.EMPTY_LIST, messages.getValues());
    }

    @Test
    void requiredUnlimitedPositionalArgumentOnMiddle_satisfied() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> receivers = new UnlimitedPositionalArgument<>(
                "receivers", StringParser.INSTANCE
        );
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John", "Samuel", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receivers);
        parser.require(message);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receivers.getValue());
        assertEquals(List.of(args[1], args[2]), receivers.getValues());
        assertEquals(args[3], message.getValue());
    }

    @Test
    void requiredUnlimitedPositionalArgumentOnMiddle_notSatisified() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> receivers = new UnlimitedPositionalArgument<>(
                "receivers", StringParser.INSTANCE
        );
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.require(receivers);
        parser.require(message);

        Exception exception = assertThrows(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing 3rd argument (message)", exception.getMessage());
    }

    @Test
    void optionalUnlimitedPositionalArgumentOnMiddle_satisfied() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> receivers = new UnlimitedPositionalArgument<>(
                "receivers", StringParser.INSTANCE
        );
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John", "Samuel", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.optional(receivers);
        parser.require(message);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receivers.getValue());
        assertEquals(List.of(args[1], args[2]), receivers.getValues());
        assertEquals(args[3], message.getValue());
    }

    @Test
    void optionalUnlimitedPositionalArgumentOnMiddle_notSatisified() {
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        UnlimitedPositionalArgument<String> receivers = new UnlimitedPositionalArgument<>(
                "receivers", StringParser.INSTANCE
        );
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "Nice Day"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(sender);
        parser.optional(receivers);
        parser.require(message);

        parser.parse(args);

        assertEquals(args[0], sender.getValue());
        assertNull(receivers.getValue());
        assertEquals(Collections.EMPTY_LIST, receivers.getValues());
        assertEquals(args[1], message.getValue());
    }

    @Test
    void requiredUnlimitedPositionalArgumentOnStart_satisfied() {
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );

        String[] args = {
                "Hi", "Nice Day", "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(messages);
        parser.require(sender);
        parser.require(receiver);

        parser.parse(args);

        assertEquals(args[0], messages.getValue());
        assertEquals(List.of(args[0], args[1]), messages.getValues());
        assertEquals(args[2], sender.getValue());
        assertEquals(args[3], receiver.getValue());
    }

    @Test
    void requiredUnlimitedPositionalArgumentOnStart_notSatisified() {
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.require(messages);
        parser.require(sender);
        parser.require(receiver);

        Exception exception = assertThrows(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing 3rd argument (receiver)", exception.getMessage());
    }

    @Test
    void optionalUnlimitedPositionalArgumentOnStart_satisfied() {
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );

        String[] args = {
                "Hi", "Nice Day", "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.optional(messages);
        parser.require(sender);
        parser.require(receiver);

        parser.parse(args);

        assertEquals(args[0], messages.getValue());
        assertEquals(List.of(args[0], args[1]), messages.getValues());
        assertEquals(args[2], sender.getValue());
        assertEquals(args[3], receiver.getValue());
    }

    @Test
    void optionalUnlimitedPositionalArgumentOnStart_notSatisified() {
        UnlimitedPositionalArgument<String> messages = new UnlimitedPositionalArgument<>(
                "messages", StringParser.INSTANCE
        );
        PositionalArgument<String> sender = new PositionalArgument<>(
                "sender", StringParser.INSTANCE
        );
        PositionalArgument<String> receiver = new PositionalArgument<>(
                "receiver", StringParser.INSTANCE
        );

        String[] args = {
                "Michael", "John"
        };

        ArgumentParser parser = new ArgumentParser();
        parser.optional(messages);
        parser.require(sender);
        parser.require(receiver);

        parser.parse(args);

        assertNull(messages.getValue());
        assertEquals(Collections.EMPTY_LIST, messages.getValues());
        assertEquals(args[0], sender.getValue());
        assertEquals(args[1], receiver.getValue());
    }

    @Test
    void nullArgument() {
        ArgumentParser parser = new ArgumentParser();
        assertThrows(NullPointerException.class, () -> parser.optional((PositionalArgument<?>)null));
        assertThrows(NullPointerException.class, () -> parser.optional((Option<?>)null));
        assertThrows(NullPointerException.class, () -> parser.require((Option<?>) null));
        assertThrows(NullPointerException.class, () -> parser.require((PositionalArgument<?>) null));
    }

    @Test
    void multipleArgumentsWithSameName_optional() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> message2 = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.optional(message);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.optional(message2));
        assertEquals("Argument with name message already exists", exception.getMessage());
    }

    @Test
    void multipleArgumentsWithSameName_required() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );
        PositionalArgument<String> message2 = new PositionalArgument<>(
                "message", StringParser.INSTANCE
        );

        ArgumentParser parser = new ArgumentParser();
        parser.require(message);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> parser.require(message2));
        assertEquals("Argument with name message already exists", exception.getMessage());
    }

    @Test
    void optionTestOptional() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"--user", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
    }

    @Test
    void optionTestOptional_shortName() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"-u", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
    }

    @Test
    void optionTestOptional_withMissingValue() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"-u"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for user", exception.getMessage());
    }

    @Test
    void optionTestOptional_noOption() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {};

        parser.parse(args);
        assertNull(user.getValue());
    }

    @Test
    void multipleValuesSuppliedForOptionOptional() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);
        String[] args = {"-u", "Andrew", "-u", "Jack"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("user only requires one value", exception.getMessage());
    }

    @Test
    void multipleOptionOptional_withSameShortName() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        Option<String> password = new Option<>("password", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);
        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> parser.optional(password));
        assertEquals("Multiple options with same short name (u) detected", exception.getMessage());
    }

    @Test
    void optionTestRequired() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"--user", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
    }

    @Test
    void optionTestRequired_shortName() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"-u", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
    }

    @Test
    void optionTestRequired_withMissingValue() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"-u"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for user", exception.getMessage());
    }

    @Test
    void optionTestRequired_noOption() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing option : user", exception.getMessage());
    }

    @Test
    void multipleValuesSuppliedForOptionRequired() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);
        String[] args = {"-u", "Andrew", "-u", "Jack"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("user only requires one value", exception.getMessage());
    }

    @Test
    void multipleOptionRequired_withSameShortName() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        Option<String> password = new Option<>("password", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);
        IllegalArgumentException exception =
                assertThrowsExactly(IllegalArgumentException.class, () -> parser.require(password));
        assertEquals("Multiple options with same short name (u) detected", exception.getMessage());
    }

    @Test
    void multipleValuesOptionTestOptional() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"--user", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
        assertEquals(List.of("Michael"), user.getValues());
    }

    @Test
    void multipleValuesOptionTestOptional_shortName() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"-u", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
        assertEquals(List.of("Michael"), user.getValues());
    }

    @Test
    void multipleValuesOptionTestOptional_withMissingValue() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {"-u"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for user", exception.getMessage());
    }

    @Test
    void multipleValuesOptionTestOptional_noOption() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);

        String[] args = {};
        parser.parse(args);

        assertNull(user.getValue());
    }

    @Test
    void multipleValuesSuppliedForMultipleValueOptionOptional() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);

        String[] args = {"-u", "Andrew", "-u", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedAsLongNameForMultipleValueOptionOptional() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);

        String[] args = {"--user", "Andrew", "--user", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedMixedForMultipleValueOptionOptional() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);

        String[] args = {"--user", "Andrew", "-u", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedForMultipleShortNamesForMultipleValueOptionOptional() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", new char[] {'u', 's'}, StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.optional(user);

        String[] args = {"-s", "Andrew", "-u", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesOptionTestRequired() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"--user", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
        assertEquals(List.of("Michael"), user.getValues());
    }

    @Test
    void multipleValuesOptionTestRequired_shortName() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"-u", "Michael"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
        assertEquals(List.of("Michael"), user.getValues());
    }

    @Test
    void multipleValuesOptionTestRequired_withMissingValue() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {"-u"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for user", exception.getMessage());
    }

    @Test
    void multipleValuesOptionTestRequired_noOption() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.require(user);

        String[] args = {};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing option : user", exception.getMessage());
    }

    @Test
    void multipleValuesSuppliedForMultipleValuesOptionRequired() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);
        String[] args = {"-u", "Andrew", "-u", "Jack"};

        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedAsLongNameForMultipleValueOptionRequired() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);

        String[] args = {"--user", "Andrew", "--user", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedMixedForMultipleValueOptionRequired() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", 'u', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);

        String[] args = {"--user", "Andrew", "-u", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void multipleValuesSuppliedForMultipleShortNamesForMultipleValueOptionRequired() {
        MultipleValueOption<String> user = new MultipleValueOption<>(
                "user", new char[] {'u', 's'}, StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();

        parser.require(user);

        String[] args = {"-s", "Andrew", "-u", "Jack"};
        parser.parse(args);

        assertEquals("Andrew", user.getValue());
        assertEquals(List.of("Andrew", "Jack"), user.getValues());
    }

    @Test
    void flagTest() {
        Flag debug = new Flag("debug", 'd');
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);

        String[] args = {"--debug"};
        parser.parse(args);

        assertTrue(debug.getValue());
    }

    @Test
    void flagTest_shortName() {
        Flag debug = new Flag("debug", 'd');
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);

        String[] args = {"-d"};
        parser.parse(args);

        assertTrue(debug.getValue());
    }

    @Test
    void flagTestWithAnPosArg() {
        Flag debug = new Flag("debug", 'd');
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);
        parser.require(message);

        String[] args = {"--debug", "Hello"};
        parser.parse(args);

        assertTrue(debug.getValue());
        assertEquals("Hello", message.getValue());
    }

    @Test
    void flagAfterDoubleHyphens() {
        Flag debug = new Flag("debug", 'd');
        PositionalArgument<String> message = new PositionalArgument<>(
                "message", StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);
        parser.require(message);

        String[] args = {"--", "--debug"};
        parser.parse(args);

        assertFalse(debug.getValue());
        assertEquals("--debug", message.getValue());
    }

    @Test
    void flagWithTwoDoubleHyphens() {
        Flag debug = new Flag("debug", 'd');
        UnlimitedPositionalArgument<String> message = new UnlimitedPositionalArgument<>(
                "message", StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);
        parser.require(message);

        String[] args = {"--", "--debug", "--"};
        parser.parse(args);

        assertFalse(debug.getValue());
        assertEquals(List.of("--debug", "--"), message.getValues());
    }

    @Test
    void flagTest_notSpecified() {
        Flag debug = new Flag("debug", 'd');
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);

        String[] args = {};
        parser.parse(args);

        assertFalse(debug.getValue());
    }

    @Test
    void flagWithValueTest() {
        Flag debug = new Flag("debug", 'd');
        Option<String> output = new Option<>("output", 'o', StringParser.INSTANCE);
        ArgumentParser parser = new ArgumentParser();
        parser.optional(debug);
        parser.require(output);

        String[] args = {"-d", "0", "--output", "hello.txt"};
        Exception e = assertThrowsExactly(ArgumentParserException.class,
                () -> parser.parse(args));
        assertEquals("Unknown value : 0", e.getMessage());
    }

    @Test
    void unknown_short_option() {
        ArgumentParser parser = new ArgumentParser();
        String[] args = {"-c", "hi"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Unknown option : c", exception.getMessage());
    }

    @Test
    void unknown_long_option() {
        ArgumentParser parser = new ArgumentParser();
        String[] args = {"--message", "hi"};

        ArgumentParserException exception =
                assertThrowsExactly(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Unknown option : message", exception.getMessage());
    }

    @Test
    void optionWithPosArgs() {
        Option<String> user = new Option<>("user", 'u', StringParser.INSTANCE);
        PositionalArgument<String> message = new PositionalArgument<>("message", StringParser.INSTANCE);

        ArgumentParser parser = new ArgumentParser();
        parser.optional(user);
        parser.require(message);

        String[] args = {"-u", "Michael", "Hi bob!"};

        parser.parse(args);
        assertEquals("Michael", user.getValue());
        assertEquals("Hi bob!", message.getValue());
    }

    @Test
    void multipleFlagsShortNameWithoutSpaces() {
        Flag print = new Flag("print", 'p');
        Flag verbose = new Flag("verbose", 'v');

        ArgumentParser parser = new ArgumentParser();
        parser.optional(print);
        parser.optional(verbose);

        String[] args = {"-vp"};

        parser.parse(args);

        assertTrue(print.getValue());
        assertTrue(verbose.getValue());
    }

    @Test
    void flagAndOptionShortNameWithoutSpace() {
        Option<String> print = new Option<>("print", 'p', StringParser.INSTANCE);
        Flag verbose = new Flag("verbose", 'v');

        ArgumentParser parser = new ArgumentParser();
        parser.optional(print);
        parser.optional(verbose);

        String[] args = {"-vp", "hi"};

        parser.parse(args);

        assertEquals("hi", print.getValue());
        assertTrue(verbose.getValue());
    }

    @Test
    void flagAndOptionShortNameWithoutSpace_withPositionalArgument() {
        PositionalArgument<String> message = new PositionalArgument<>(
                "message",StringParser.INSTANCE);
        Option<String> print = new Option<>("print", 'p', StringParser.INSTANCE);
        Flag verbose = new Flag("verbose", 'v');

        ArgumentParser parser = new ArgumentParser();
        parser.optional(message);
        parser.optional(print);
        parser.optional(verbose);

        String[] args = {"-vp", "hi", "hi2"};

        parser.parse(args);

        assertEquals("hi", print.getValue());
        assertEquals("hi2", message.getValue());
        assertTrue(verbose.getValue());
    }

    @Test
    void flagAndOptionShortNameWithoutSpace_optionNotSupplied() {
        Option<String> print = new Option<>("print", 'p', StringParser.INSTANCE);
        Flag verbose = new Flag("verbose", 'v');

        ArgumentParser parser = new ArgumentParser();
        parser.optional(print);
        parser.optional(verbose);

        String[] args = {"-vp"};

        ArgumentParserException exception =
                assertThrows(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for print", exception.getMessage());
    }

    @Test
    void flagAndOptionShortNameWithoutSpace_optionOnStart() {
        Option<String> print = new Option<>("print", 'p', StringParser.INSTANCE);
        Flag verbose = new Flag("verbose", 'v');

        ArgumentParser parser = new ArgumentParser();
        parser.optional(print);
        parser.optional(verbose);

        String[] args = {"-pv"};

        ArgumentParserException exception =
                assertThrows(ArgumentParserException.class, () -> parser.parse(args));
        assertEquals("Missing value for print", exception.getMessage());
    }
}
