package com.abiddarris.common.renpy.rpy;

import static com.abiddarris.common.renpy.internal.Builtins.None;

import java.io.File;
import java.io.IOException;

public class RpyMain {
    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        try {
            Rpy.initLoader();

            RpyDecompiler.decompile(new File("/home/abid/Programming/just yuri/splash.rpyc"), false, true, None);
        } finally {
            System.err.println(System.currentTimeMillis() - startTime);
        }
    }

}
