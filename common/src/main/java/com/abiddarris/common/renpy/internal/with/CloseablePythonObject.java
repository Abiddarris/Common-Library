package com.abiddarris.common.renpy.internal.with;

import static com.abiddarris.common.renpy.internal.PythonObject.None;

import com.abiddarris.common.renpy.internal.PythonObject;

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
