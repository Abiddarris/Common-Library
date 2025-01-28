package com.abiddarris.unrpyc;

import static com.abiddarris.python3.Builtins.None;

import com.abiddarris.python3.PythonObject;

import java.io.File;
import java.io.IOException;

public class RpyDecompiler {

    public static Unrpyc.Context decompile(
            File file,
            boolean overwrite,
            boolean initOffset,
            PythonObject slCustomNames) throws IOException {

        Unrpyc.Context context = new Unrpyc.Context();
        Unrpyc.decompile_rpyc(file,
                context, overwrite,
                false, false, false, false, None, initOffset, slCustomNames);
        return context;
    }
}