package com.abiddarris.python3.with;

import static com.abiddarris.python3.Builtins.None;

import com.abiddarris.python3.PythonObject;

import java.io.Closeable;
import java.io.IOException;

class CloseablePythonObject implements Closeable  {

    private PythonObject object;

    CloseablePythonObject(PythonObject object) {
        this.object = object;
    }

    @Override
    public void close() throws IOException {
        object.callAttribute("__exit__", None, None, None);
    }
}
