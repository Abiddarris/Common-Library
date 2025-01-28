package com.abiddarris.python3.mod.operator;

import static com.abiddarris.python3.Builtins.__import__;
import static com.abiddarris.python3.Python.newInt;
import static com.abiddarris.python3.Python.newString;
import static com.abiddarris.python3.Python.newTuple;
import static org.junit.jupiter.api.Assertions.*;

import com.abiddarris.python3.PythonObject;

import org.junit.jupiter.api.Test;

class OperatorImplTest {

    @Test
    void operatorItemGetter() {
        PythonObject operator = __import__.call(newString("operator"));
        PythonObject itemgetter = operator.getAttribute("itemgetter");

        PythonObject igetter = itemgetter.call(newInt(3));
        PythonObject tuple = newTuple(newString("Hello"), newString("World"), newString("Python"), newString("Java"));

        assertEquals(newString("Java"), igetter.call(tuple));
    }

}