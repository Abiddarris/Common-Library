package com.abiddarris.python3.builtins;

import static com.abiddarris.python3.Builtins.sorted;
import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newList;
import static com.abiddarris.python3.Python.newTuple;
import static com.abiddarris.python3.core.functions.Functions.newFunction;

import com.abiddarris.python3.PythonObject;
import com.abiddarris.python3.core.functions.PFunction;

import org.junit.jupiter.api.Test;

public class SortedTest {

    @Test
    void sort() {
        PythonObject list = newList(
                newTuple(newInt(4), newInt(16)),
                newTuple(newInt(7), newInt(49)),
                newTuple(newInt(2), newInt(4)),
                newTuple(newInt(12), newInt(144)),
                newTuple(newInt(3), newInt(9)),
                newTuple(newInt(10), newInt(100))
        );

        list = sorted.call(list, newFunction((PFunction) x -> x.getItem(0), "x"));

        System.out.println(list);
    }

}
