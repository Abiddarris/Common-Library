package com.abiddarris.python3.core;

import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.Builtins.False;
import static com.abiddarris.python3.Builtins.True;
import static com.abiddarris.python3.core.Functions.all;
import static com.abiddarris.python3.core.Functions.max;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FunctionsTest {

    @Test
    void all_true() {
        assertEquals(True, all(newTuple(True, newInt(1), newString("something"))));
    }

    @Test
    void all_false() {
        assertEquals(False, all(newTuple(True, newInt(0), newString("something"))));
    }

    @Test
    void max_test() {
        assertEquals(newInt(4), max(newInt(3), newInt(4)));
    }
}