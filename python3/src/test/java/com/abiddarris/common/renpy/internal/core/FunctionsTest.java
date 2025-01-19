package com.abiddarris.common.renpy.internal.core;

import static com.abiddarris.common.renpy.internal.Python.newInt;
import static com.abiddarris.common.renpy.internal.Python.newString;
import static com.abiddarris.common.renpy.internal.Python.newTuple;
import static com.abiddarris.common.renpy.internal.Builtins.False;
import static com.abiddarris.common.renpy.internal.Builtins.True;
import static com.abiddarris.common.renpy.internal.core.Functions.all;
import static com.abiddarris.common.renpy.internal.core.Functions.max;
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