package com.abiddarris.common.renpy.rpy;

import static com.abiddarris.common.renpy.internal.Builtins.None;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RpyMain {
    public static void main(String[] args) throws IOException {
//        System.err.print("Press any key to continue");
//        System.in.read();

        long startTime = System.currentTimeMillis();
        try {
            Rpy.initLoader();

            Unrpyc.decompile_rpyc(new File("/home/abid/Programming/just yuri/" + args[0] + ".rpyc"), new Unrpyc.Context(), false,
                    false, false, false, false, None, true, None);
        } finally {
            System.err.println(System.currentTimeMillis() - startTime);
        }
    }


}
